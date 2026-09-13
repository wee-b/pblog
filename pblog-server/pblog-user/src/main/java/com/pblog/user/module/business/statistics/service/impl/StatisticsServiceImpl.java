package com.pblog.user.module.business.statistics.service.impl;

import com.pblog.common.domain.vo.DailyVisitVO;
import com.pblog.common.domain.vo.StatisticsOverviewVO;
import com.pblog.user.module.business.statistics.mapper.StatisticsMapper;
import com.pblog.user.module.business.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private static final String VISIT_FINGERPRINT_KEY_PREFIX = "statistics:visit:fingerprint:";
    private static final Duration VISIT_DEDUPLICATION_TTL = Duration.ofHours(8);

    private final StatisticsMapper statisticsMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean recordVisit(String fingerprint) {
        String redisKey = VISIT_FINGERPRINT_KEY_PREFIX + sha256(fingerprint.trim());
        Boolean firstVisit = stringRedisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", VISIT_DEDUPLICATION_TTL);

        if (!Boolean.TRUE.equals(firstVisit)) {
            return false;
        }

        try {
            statisticsMapper.incrementDailyVisit(LocalDate.now());
            return true;
        } catch (RuntimeException exception) {
            // 数据库写入失败时释放去重键，允许客户端稍后重试，避免永久少记一次。
            stringRedisTemplate.delete(redisKey);
            throw exception;
        }
    }

    @Override
    public StatisticsOverviewVO getOverview(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1L);
        List<DailyVisitVO> storedTrend = statisticsMapper.selectVisitTrend(startDate, endDate);

        Map<LocalDate, Long> visitsByDate = new LinkedHashMap<>();
        for (int offset = 0; offset < days; offset++) {
            visitsByDate.put(startDate.plusDays(offset), 0L);
        }
        storedTrend.forEach(item -> visitsByDate.put(item.getDate(), item.getVisitCount()));

        StatisticsOverviewVO overview = new StatisticsOverviewVO();
        overview.setTotalViews(statisticsMapper.selectTotalViews());
        overview.setTodayViews(visitsByDate.getOrDefault(endDate, 0L));
        overview.setTotalArticles(statisticsMapper.selectPublishedArticleCount());
        overview.setTotalComments(statisticsMapper.selectApprovedCommentCount());
        overview.setVisitTrend(visitsByDate.entrySet().stream()
                .map(entry -> new DailyVisitVO(entry.getKey(), entry.getValue()))
                .toList());
        return overview;
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前运行环境不支持SHA-256摘要算法", exception);
        }
    }
}
