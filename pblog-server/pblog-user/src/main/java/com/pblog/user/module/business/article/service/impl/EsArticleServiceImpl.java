package com.pblog.user.module.business.article.service.impl;

import com.pblog.common.domain.dto.Article.ArticlePageQueryDTO;
import com.pblog.common.domain.entity.Article;
import com.pblog.common.domain.result.PageResult;
import com.pblog.common.domain.vo.ArticleVO;
import com.pblog.common.domain.vo.CategoryVO;
import com.pblog.user.module.business.article.esEntity.EsArticle;
import com.pblog.user.module.business.article.mapper.EsArticleRepository;
import com.pblog.user.module.business.article.service.EsArticleService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EsArticleServiceImpl implements EsArticleService {

    @Resource
    private EsArticleRepository esArticleRepository;

    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public PageResult pageQuery(ArticlePageQueryDTO dto) {

        // 1 默认参数处理
        if (dto == null) {
            dto = new ArticlePageQueryDTO();
        }

        int pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        int pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();

        // 2 keyword 处理（允许为空）
        String keyword = StringUtils.hasText(dto.getKeyword()) ? dto.getKeyword() : "";

        // 3 分类ID转换
        List<String> categoryIds = CollectionUtils.isEmpty(dto.getCategoryIds())
                ? List.of()
                : dto.getCategoryIds()
                .stream()
                .map(String::valueOf)
                .collect(Collectors.toList());

        // 4 分页对象（ES分页从0开始）
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        // 5 调用ES查询
        Page<EsArticle> esPage = esArticleRepository.search(
                keyword,
                categoryIds,
                pageable
        );

        // 6 转换VO
        List<ArticleVO> list = esPage.getContent()
                .stream()
                .map(this::convertToArticleVO)
                .collect(Collectors.toList());

        // 7 构建分页结果
        return new PageResult(
                esPage.getTotalElements(),
                (long) esPage.getTotalPages(),
                (long) pageNum,
                (long) pageSize,
                list
        );
    }


    /**
     * 新增/更新 ES
     */
    @Override
    public void syncArticle(EsArticle article){
        esArticleRepository.save(article);
    }

    /**
     * 删除 ES
     */
    @Override
    public void deleteArticle(Integer id){
        esArticleRepository.deleteById(id);
    }


    @Override
    public void updateArticleTags(Integer articleId, String[] tagIds, boolean isAddOperation) {
        if (tagIds.length == 0 || articleId == null) {
            return;
        }

        String script;
        if (isAddOperation) {
            // 使用Set集合可以自动去重
            script = """
                if (ctx._source.containsKey('articleTagIds')) {
                  ctx._source.articleTagIds.addAll(params.tagIds);
                } else {
                  ctx._source.articleTagIds = params.tagIds;
                }
                """;
        } else {
            script = """
                if (ctx._source.containsKey('articleTagIds')) {
                  ctx._source.articleTagIds.removeAll(params.tagIds);
                }
                """;
        }

        UpdateQuery updateQuery = UpdateQuery.builder(String.valueOf(articleId))
                .withScript(script)
                .withLang("painless")
                .withParams(java.util.Collections.singletonMap("tagIds", tagIds))
                .build();

        // 使用 elasticsearchOperations 执行更新操作
        elasticsearchOperations.update(updateQuery, IndexCoordinates.of("pb_article"));
    }

    /**
     * EsArticle → ArticleVO
     */
    private ArticleVO convertToArticleVO(EsArticle esArticle) {

        ArticleVO articleVO = new ArticleVO();

        articleVO.setId(esArticle.getId());
        articleVO.setTitle(esArticle.getTitle());
        articleVO.setSummary(esArticle.getSummary());
        articleVO.setCoverImage(esArticle.getCoverImage());

        articleVO.setAuthorUsername(esArticle.getAuthorUsername());
        articleVO.setAuthorNickName(esArticle.getAuthorNickName());

        articleVO.setViewCount(esArticle.getViewCount());
        articleVO.setLikeCount(esArticle.getLikeCount());
        articleVO.setCommentCount(esArticle.getCommentCount());

        articleVO.setSticky(esArticle.getSticky());
        articleVO.setFeatured(esArticle.getFeatured());

        articleVO.setPublishedAt(esArticle.getPublishedAt());
        articleVO.setStatus(esArticle.getStatus());

        // 分类转换（ES直接读取）
        String[] tagIds = esArticle.getArticleTagIds();
        String[] tagNames = esArticle.getCategoryName();

        if (tagIds != null && tagIds.length > 0) {

            List<CategoryVO> categories = new ArrayList<>();

            for (int i = 0; i < tagIds.length; i++) {

                CategoryVO categoryVO = new CategoryVO();
                categoryVO.setId(Integer.valueOf(tagIds[i]));
                if (tagNames != null && tagNames.length > i) {
                    categoryVO.setCategoryName(tagNames[i]);
                }

                categories.add(categoryVO);
            }
            articleVO.setCategories(categories);
        }

        return articleVO;
    }

    /**
     * Article → EsArticle
     */
    private EsArticle convert(Article article){
        EsArticle es = new EsArticle();
        es.setId(article.getId());
        es.setTitle(article.getTitle());
        es.setContent(article.getContent());
        es.setSummary(article.getSummary());
        es.setCoverImage(article.getCoverImage());
        es.setAuthorUsername(article.getAuthorUsername());
        es.setAuthorNickName(article.getAuthorNickName());
        es.setStatus(article.getStatus());
        es.setViewCount(article.getViewCount());
        es.setLikeCount(article.getLikeCount());
        es.setCommentCount(article.getCommentCount());
        es.setSticky(article.getSticky());
        es.setFeatured(article.getFeatured());
        es.setPublishedAt(article.getPublishedAt());
        es.setDelFlag(article.getDelFlag());
        return es;
    }


}