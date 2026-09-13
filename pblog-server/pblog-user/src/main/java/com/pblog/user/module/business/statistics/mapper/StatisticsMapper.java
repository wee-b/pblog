package com.pblog.user.module.business.statistics.mapper;

import com.pblog.common.domain.vo.DailyVisitVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StatisticsMapper {

    @Insert("""
            INSERT INTO pb_daily_visit_statistics (statistic_date, visit_count)
            VALUES (#{date}, 1)
            ON DUPLICATE KEY UPDATE visit_count = visit_count + 1
            """)
    int incrementDailyVisit(@Param("date") LocalDate date);

    @Select("SELECT COALESCE(SUM(visit_count), 0) FROM pb_daily_visit_statistics")
    Long selectTotalViews();

    @Select("""
            SELECT statistic_date AS date, visit_count AS visitCount
            FROM pb_daily_visit_statistics
            WHERE statistic_date BETWEEN #{startDate} AND #{endDate}
            ORDER BY statistic_date ASC
            """)
    List<DailyVisitVO> selectVisitTrend(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    @Select("""
            SELECT COUNT(*) FROM pb_article
            WHERE status = '1' AND del_flag = '0'
            """)
    Long selectPublishedArticleCount();

    @Select("""
            SELECT COUNT(*) FROM pb_comment
            WHERE status = '1' AND del_flag = '0'
            """)
    Long selectApprovedCommentCount();
}
