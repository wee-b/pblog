package com.pblog.user.module.business.series.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pblog.common.domain.dto.SeriesDTO;
import com.pblog.common.domain.dto.SeriesPageQueryDTO;
import com.pblog.common.domain.entity.Series;
import com.pblog.common.domain.result.PageResult;
import com.pblog.common.domain.vo.ArticleVO;
import com.pblog.common.domain.vo.SeriesDetailVO;
import com.pblog.common.domain.vo.SeriesVO;

import java.util.List;

public interface SeriesService extends IService<Series> {
    PageResult pageQuery(SeriesPageQueryDTO queryDTO);
    List<SeriesVO> featured(int limit);
    SeriesDetailVO queryById(Integer id);
    PageResult myPageQuery(SeriesPageQueryDTO queryDTO);
    SeriesDetailVO queryMineById(Integer id);
    List<ArticleVO> candidateArticles();
    Integer insert(SeriesDTO dto);
    boolean update(Integer id, SeriesDTO dto);
    boolean delete(Integer id);
}
