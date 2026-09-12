package com.pblog.user.module.business.series.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pblog.common.Expection.BusinessException;
import com.pblog.common.domain.dto.SeriesDTO;
import com.pblog.common.domain.dto.SeriesPageQueryDTO;
import com.pblog.common.domain.entity.Article;
import com.pblog.common.domain.entity.Series;
import com.pblog.common.domain.result.PageResult;
import com.pblog.common.domain.vo.AcRelationVO;
import com.pblog.common.domain.vo.ArticleVO;
import com.pblog.common.domain.vo.CategoryVO;
import com.pblog.common.domain.vo.SeriesArticleVO;
import com.pblog.common.domain.vo.SeriesDetailVO;
import com.pblog.common.domain.vo.SeriesVO;
import com.pblog.common.storage.StorageUrlResolver;
import com.pblog.common.utils.SecurityContextUtil;
import com.pblog.user.module.business.article.mapper.ACRelationMapper;
import com.pblog.user.module.business.article.mapper.ArticleMapper;
import com.pblog.user.module.business.series.mapper.SaRelationMapper;
import com.pblog.user.module.business.series.mapper.SeriesMapper;
import com.pblog.user.module.business.series.service.SeriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeriesServiceImpl extends ServiceImpl<SeriesMapper, Series> implements SeriesService {

    private final SeriesMapper seriesMapper;
    private final SaRelationMapper saRelationMapper;
    private final ArticleMapper articleMapper;
    private final ACRelationMapper acRelationMapper;
    private final StorageUrlResolver storageUrlResolver;

    @Override
    public PageResult pageQuery(SeriesPageQueryDTO queryDTO) {
        return selectPage(queryDTO, null, false);
    }

    @Override
    public List<SeriesVO> featured(int limit) {
        SeriesPageQueryDTO queryDTO = new SeriesPageQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(Math.max(1, Math.min(limit, 8)));
        return castSeriesRecords(selectPage(queryDTO, null, false).getRecords());
    }

    @Override
    public SeriesDetailVO queryById(Integer id) {
        return buildDetail(id, null, false, true);
    }

    @Override
    public PageResult myPageQuery(SeriesPageQueryDTO queryDTO) {
        return selectPage(queryDTO, SecurityContextUtil.getUsername(), true);
    }

    @Override
    public SeriesDetailVO queryMineById(Integer id) {
        return buildDetail(id, SecurityContextUtil.getUsername(), true, false);
    }

    @Override
    public List<ArticleVO> candidateArticles() {
        List<ArticleVO> articles = seriesMapper.selectCandidateArticles(SecurityContextUtil.getUsername());
        fillArticleMetadata(articles);
        return articles;
    }

    @Transactional
    @Override
    public Integer insert(SeriesDTO dto) {
        String username = SecurityContextUtil.getUsername();
        ensureUniqueName(dto.getSeriesName(), username, null);

        Series series = new Series();
        BeanUtils.copyProperties(dto, series);
        series.setSeriesName(dto.getSeriesName().trim());
        series.setDescription(normalizeDescription(dto.getDescription()));
        series.setStatus("0");
        series.setDelFlag("0");
        series.setAuthorUsername(username);
        series.setCreateTime(LocalDateTime.now());
        series.setUpdateTime(LocalDateTime.now());
        seriesMapper.insert(series);

        replaceRelations(series.getId(), dto.getArticleIds(), username);
        return series.getId();
    }

    @Transactional
    @Override
    public boolean update(Integer id, SeriesDTO dto) {
        Series existing = requireOwnedSeries(id);
        String username = SecurityContextUtil.getUsername();
        ensureUniqueName(dto.getSeriesName(), username, id);

        existing.setSeriesName(dto.getSeriesName().trim());
        existing.setDescription(normalizeDescription(dto.getDescription()));
        existing.setCoverFileId(dto.getCoverFileId());
        existing.setUpdateTime(LocalDateTime.now());
        int rows = seriesMapper.updateById(existing);
        if (rows == 0) throw new BusinessException("合集修改失败");

        replaceRelations(id, dto.getArticleIds(), username);
        return true;
    }

    @Transactional
    @Override
    public boolean delete(Integer id) {
        requireOwnedSeries(id);

        LambdaUpdateWrapper<Series> update = new LambdaUpdateWrapper<>();
        update.eq(Series::getId, id)
                .eq(Series::getDelFlag, "0")
                .set(Series::getDelFlag, "1")
                .set(Series::getUpdateTime, LocalDateTime.now());
        if (seriesMapper.update(null, update) == 0) {
            throw new BusinessException("合集删除失败");
        }

        saRelationMapper.physicalDeleteBySeriesId(id);
        return true;
    }

    private PageResult selectPage(
            SeriesPageQueryDTO queryDTO,
            String username,
            boolean includeDisabled) {
        if (queryDTO == null) queryDTO = new SeriesPageQueryDTO();
        Page<SeriesVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<SeriesVO> result = seriesMapper.selectSeriesPage(
                page, queryDTO, username, includeDisabled);
        result.getRecords().forEach(storageUrlResolver::resolveSeries);
        return new PageResult(
                result.getTotal(), result.getPages(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    private SeriesDetailVO buildDetail(
            Integer id,
            String username,
            boolean includeDisabled,
            boolean publishedOnly) {
        SeriesVO series = seriesMapper.selectSeriesVOById(id, username, includeDisabled);
        if (series == null) throw new BusinessException("合集不存在或已被删除");
        storageUrlResolver.resolveSeries(series);

        List<SeriesArticleVO> articles = seriesMapper.selectSeriesArticles(id, publishedOnly);
        fillArticleMetadata(new ArrayList<>(articles));

        SeriesDetailVO detail = new SeriesDetailVO();
        BeanUtils.copyProperties(series, detail);
        detail.setArticles(articles);
        return detail;
    }

    private void fillArticleMetadata(List<? extends ArticleVO> articles) {
        if (articles == null || articles.isEmpty()) return;
        List<Integer> articleIds = articles.stream().map(ArticleVO::getId).toList();
        Map<Integer, List<CategoryVO>> categories = acRelationMapper
                .selectAcRelationVOByArticleIds(articleIds)
                .stream()
                .collect(Collectors.toMap(
                        AcRelationVO::getArticleId,
                        AcRelationVO::getCategoryList,
                        (left, right) -> left));
        articles.forEach(article -> {
            article.setCategories(categories.getOrDefault(article.getId(), Collections.emptyList()));
            storageUrlResolver.resolveArticle(article);
        });
    }

    private void replaceRelations(Integer seriesId, List<Integer> articleIds, String username) {
        Set<Integer> uniqueIds = new LinkedHashSet<>(
                articleIds == null ? Collections.emptyList() : articleIds);
        if (uniqueIds.size() > 100) throw new BusinessException("单个合集最多支持100篇文章");

        if (!uniqueIds.isEmpty()) {
            List<Article> articles = articleMapper.selectBatchIds(uniqueIds);
            Map<Integer, Article> articleMap = articles.stream()
                    .collect(Collectors.toMap(Article::getId, Function.identity()));
            boolean invalid = uniqueIds.stream().anyMatch(articleId -> {
                Article article = articleMap.get(articleId);
                return article == null
                        || !Objects.equals(username, article.getAuthorUsername())
                        || "1".equals(article.getDelFlag());
            });
            if (invalid) throw new BusinessException("合集只能添加自己未删除的文章");
        }

        saRelationMapper.physicalDeleteBySeriesId(seriesId);
        if (!uniqueIds.isEmpty()) {
            saRelationMapper.batchInsert(seriesId, new ArrayList<>(uniqueIds));
        }
    }

    private Series requireOwnedSeries(Integer id) {
        Series series = seriesMapper.selectOne(new LambdaQueryWrapper<Series>()
                .eq(Series::getId, id)
                .eq(Series::getAuthorUsername, SecurityContextUtil.getUsername())
                .eq(Series::getDelFlag, "0"));
        if (series == null) throw new BusinessException("合集不存在或无权操作");
        return series;
    }

    private void ensureUniqueName(String seriesName, String username, Integer excludeId) {
        LambdaQueryWrapper<Series> wrapper = new LambdaQueryWrapper<Series>()
                .eq(Series::getSeriesName, seriesName.trim())
                .eq(Series::getAuthorUsername, username)
                .eq(Series::getDelFlag, "0");
        if (excludeId != null) wrapper.ne(Series::getId, excludeId);
        if (seriesMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("你已经创建过同名合集");
        }
    }

    private String normalizeDescription(String description) {
        return description == null ? "" : description.trim();
    }

    @SuppressWarnings("unchecked")
    private List<SeriesVO> castSeriesRecords(List<?> records) {
        return (List<SeriesVO>) records;
    }
}
