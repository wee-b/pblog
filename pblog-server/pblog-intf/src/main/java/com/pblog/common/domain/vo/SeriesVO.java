package com.pblog.common.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeriesVO {
    private Integer id;
    private String seriesName;
    private Long coverFileId;
    private String coverImage;
    private String description;
    private String status;
    private String authorUsername;
    private Long articleCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
