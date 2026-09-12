package com.pblog.user.module.business.series.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pblog.common.domain.dto.SeriesPageQueryDTO;
import com.pblog.common.domain.entity.Series;
import com.pblog.common.domain.vo.ArticleVO;
import com.pblog.common.domain.vo.SeriesArticleVO;
import com.pblog.common.domain.vo.SeriesVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SeriesMapper extends BaseMapper<Series> {

    IPage<SeriesVO> selectSeriesPage(
            @Param("page") Page<SeriesVO> page,
            @Param("query") SeriesPageQueryDTO query,
            @Param("username") String username,
            @Param("includeDisabled") boolean includeDisabled
    );

    SeriesVO selectSeriesVOById(
            @Param("id") Integer id,
            @Param("username") String username,
            @Param("includeDisabled") boolean includeDisabled
    );

    List<SeriesArticleVO> selectSeriesArticles(
            @Param("seriesId") Integer seriesId,
            @Param("publishedOnly") boolean publishedOnly
    );

    List<ArticleVO> selectCandidateArticles(@Param("username") String username);
}
