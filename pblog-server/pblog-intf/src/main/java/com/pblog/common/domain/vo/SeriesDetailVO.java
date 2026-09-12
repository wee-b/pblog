package com.pblog.common.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SeriesDetailVO extends SeriesVO {
    private List<SeriesArticleVO> articles = new ArrayList<>();
}
