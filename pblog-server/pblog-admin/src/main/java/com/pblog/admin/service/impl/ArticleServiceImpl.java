package com.pblog.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pblog.common.constant.DefaultConstants;
import com.pblog.common.domain.dto.Article.ArticleDTO;
import com.pblog.common.domain.dto.Article.ArticlePageQueryDTO;
import com.pblog.common.domain.dto.Article.updateArticleDTO;
import com.pblog.common.domain.entity.AcRelation;
import com.pblog.common.domain.entity.Article;
import com.pblog.common.domain.result.PageResult;
import com.pblog.common.utils.SecurityContextUtil;
import com.pblog.common.domain.vo.AcRelationVO;
import com.pblog.common.domain.vo.ArticleDetailVO;
import com.pblog.common.domain.vo.ArticleVO;
import com.pblog.common.domain.vo.CategoryVO;
import com.pblog.common.storage.StorageUrlResolver;
import com.pblog.admin.mapper.ACRelationMapper;
import com.pblog.admin.mapper.ArticleMapper;
import com.pblog.admin.service.ArticleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 博客文章表(Article)表服务实现类
 *
 * @author makejava
 * @since 2025-11-29 01:25:36
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private ACRelationMapper acRelationMapper;
    @Resource
    private StorageUrlResolver storageUrlResolver;

    @Override
    public PageResult pageQuery(ArticlePageQueryDTO pageQueryDTO) {
        // 1. 入参防御：默认分页参数（pageNum=1，pageSize=10）
        if (pageQueryDTO == null) {
            pageQueryDTO = new ArticlePageQueryDTO();
        }

        // 2. MP 分页对象：自动处理 limit 分页（依赖分页插件）
        Page<ArticleVO> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());

        // 3. 调用 MyBatis XML 自定义查询（SQL 逻辑在 XML 中，可读性强）
        IPage<ArticleVO> articlePage = articleMapper.selectArticlePage(page, pageQueryDTO);
        List<ArticleVO> articles = articlePage.getRecords();
        List<Integer> articleIds = articles.stream()
                .map(ArticleVO::getId).toList();

        // 4. 查询文章对应分类并直接构建 Map
        Map<Integer, List<CategoryVO>> articleIdToCategories = acRelationMapper
                .selectAcRelationVOByArticleIds(articleIds)
                .stream()
                .collect(Collectors.toMap(AcRelationVO::getArticleId, AcRelationVO::getCategoryList));

        // 5. 遍历文章列表一次就赋值
        articles.forEach(article -> {
            article.setCategories(articleIdToCategories.getOrDefault(article.getId(), Collections.emptyList()));
            storageUrlResolver.resolveArticle(article);
        });

        // 6. 构建完整分页结果
        return new PageResult(
                articlePage.getTotal(),    // 总记录数
                articlePage.getPages(),    // 总页数
                articlePage.getCurrent(),  // 当前页码
                articlePage.getSize(),     // 每页条数
                articles                // 当前页数据
        );
    }

    /**
     * 保存草稿、发布文章
     */
    @Transactional
    @Override
    public Integer insert(ArticleDTO articledto,String url) {

        Article article = new Article();
        BeanUtils.copyProperties(articledto, article);

        String status = DefaultConstants.Already_handout;
        if ("/insertDraft".equals(url)) {
            status = DefaultConstants.Draft_Status;
            article.setCoverFileId(null);
        } else {
            article.setPublishedAt(LocalDateTime.now());
        }

        // 可调整
        article.setCommentCount(DefaultConstants.ZERO);
        article.setLikeCount(DefaultConstants.ZERO);
        article.setViewCount(DefaultConstants.ZERO);
        article.setDelFlag(DefaultConstants.DEFAULT_DELFLAG);
        article.setSticky(DefaultConstants.unSticky);
        article.setFeatured(DefaultConstants.unFeatured);
        article.setAuthorUsername(SecurityContextUtil.getUsername());
        article.setAuthorNickName(SecurityContextUtil.getUser().getNickname());
        article.setStatus(status);

        articleMapper.insert(article);

        if (articledto.getTags() != null && !articledto.getTags().isEmpty()) {
            // 插入标签数据
            List<Integer> tagIds = articledto.getTags().stream()
                    .map(CategoryVO::getId)
                    .toList();
            acRelationMapper.insertByCategoryIds(article.getId(), tagIds);
        }


        return article.getId();
    }

    @Transactional
    @Override
    public boolean update(updateArticleDTO articledto) {

        LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<>();
        // 可调整
        updateWrapper.eq(Article::getId, articledto.getId())
                .set(Article::getTitle, articledto.getTitle())
                .set(Article::getContent, articledto.getContent())
                .set(Article::getSummary, articledto.getSummary())
                .set(Article::getPublishedAt, LocalDateTime.now())
                .set(Article::getStatus, DefaultConstants.Already_handout);

        // 保存草稿不会携带封面文件 ID
        if (articledto.getCoverFileId() != null) {
            updateWrapper.set(Article::getCoverFileId, articledto.getCoverFileId());
        }

        // getTagIds空指针判断
        if (articledto.getTags() != null && !articledto.getTags().isEmpty()) {
            // 删除该文章的所有旧关联
            LambdaQueryWrapper<AcRelation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AcRelation::getArticleId, articledto.getId());
            acRelationMapper.delete(wrapper);
            // 再插入新关联
            List<Integer> tagIds = articledto.getTags().stream()
                    .map(CategoryVO::getId)
                    .toList();
            acRelationMapper.insertByCategoryIds(articledto.getId(), tagIds);
        }

        int rows = articleMapper.update(null, updateWrapper);
        return rows > 0;
    }


    @Override
    public boolean statusPass(Integer id) {

        String status = DefaultConstants.Already_handout;
        Article one = articleMapper.selectById(id);

        LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<>();
        // 更新条件：根据 id 定位（必须，否则会更新所有数据！）
        updateWrapper.eq(Article::getId, one.getId())
                .set(Article::getStatus,status)
                .set(Article::getPublishedAt, LocalDateTime.now());

        int rows = articleMapper.update(null, updateWrapper);
        return rows > 0;
    }


    @Override
    public boolean status(Integer id) {

        String status = "";
        Article one = articleMapper.selectById(id);

        if(one.getStatus().equals(DefaultConstants.Already_handout)){
            // 已发布-->草稿  （下架）
            status = DefaultConstants.Draft_Status;
        }else if(one.getStatus().equals(DefaultConstants.Draft_Status)
                || one.getStatus().equals(DefaultConstants.toInspect)){
            status = DefaultConstants.Already_handout;
        }


        LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<>();
        // 更新条件：根据 id 定位（必须，否则会更新所有数据！）
        updateWrapper.eq(Article::getId, one.getId())
                .set(Article::getStatus,status);
        if (DefaultConstants.Already_handout.equals(status)) {
            updateWrapper.set(Article::getPublishedAt, LocalDateTime.now());
        }

        int rows = articleMapper.update(null, updateWrapper);
        return rows > 0;
    }

    @Override
    public boolean setSticky(Integer id) {
        Article one = articleMapper.selectById(id);
        // 可调整
        String banned = DefaultConstants.unSticky;      //1
        String unban = DefaultConstants.isSticky;

        LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<>();
        // 更新条件：根据 id 定位（必须，否则会更新所有数据！）
        updateWrapper.eq(Article::getId, one.getId())
                .set(Article::getSticky, one.getSticky().equals(banned) ? unban : banned);

        int rows = articleMapper.update(null, updateWrapper);
        return rows > 0;
    }

    @Override
    public boolean setFeatured(Integer id) {
        Article one = articleMapper.selectById(id);
        // 可调整
        String banned = DefaultConstants.unFeatured;
        String unban = DefaultConstants.isFeatured;

        LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<>();
        // 更新条件：根据 id 定位（必须，否则会更新所有数据！）
        updateWrapper.eq(Article::getId, one.getId())
                .set(Article::getFeatured, one.getFeatured().equals(banned) ? unban : banned);

        int rows = articleMapper.update(null, updateWrapper);
        return rows > 0;
    }

    @Override
    public List<ArticleVO> getFeaturedArticles() {
        List<ArticleVO> articlevos = articleMapper.selectFeaturedArticle();
        articlevos.forEach(storageUrlResolver::resolveArticle);
        return articlevos;
    }

    @Override
    public ArticleDetailVO queryById(Integer id) {
        ArticleDetailVO article = articleMapper.getArticleDetail(id);
        storageUrlResolver.resolveArticle(article);
        return article;
    }

}

