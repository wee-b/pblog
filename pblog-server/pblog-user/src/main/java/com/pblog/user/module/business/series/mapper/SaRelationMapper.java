package com.pblog.user.module.business.series.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pblog.common.domain.entity.SaRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SaRelationMapper extends BaseMapper<SaRelation> {
    int physicalDeleteBySeriesId(@Param("seriesId") Integer seriesId);

    int batchInsert(
            @Param("seriesId") Integer seriesId,
            @Param("articleIds") List<Integer> articleIds
    );
}
