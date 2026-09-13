package com.pblog.user.module.business.statistics.controller;

import com.pblog.common.domain.dto.VisitRecordDTO;
import com.pblog.common.domain.result.ResponseResult;
import com.pblog.common.domain.vo.StatisticsOverviewVO;
import com.pblog.user.module.business.statistics.service.StatisticsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @PostMapping("/visit")
    public ResponseResult<Boolean> recordVisit(@Valid @RequestBody VisitRecordDTO request) {
        return ResponseResult.success(statisticsService.recordVisit(request.getFingerprint()));
    }

    @GetMapping("/overview")
    public ResponseResult<StatisticsOverviewVO> getOverview(
            @RequestParam(defaultValue = "7") @Min(1) @Max(30) int days) {
        return ResponseResult.success(statisticsService.getOverview(days));
    }
}
