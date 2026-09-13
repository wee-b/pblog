package com.pblog.user.module.business.statistics.service;

import com.pblog.common.domain.vo.StatisticsOverviewVO;

public interface StatisticsService {

    boolean recordVisit(String fingerprint);

    StatisticsOverviewVO getOverview(int days);
}
