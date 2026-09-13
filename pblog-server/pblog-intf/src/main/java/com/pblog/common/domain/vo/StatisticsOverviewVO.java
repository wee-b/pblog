package com.pblog.common.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class StatisticsOverviewVO {

    private Long totalViews;
    private Long todayViews;
    private Long totalArticles;
    private Long totalComments;
    private List<DailyVisitVO> visitTrend;
}
