package com.pblog.common.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SeriesDTO {

    @NotBlank(message = "合集名称不能为空")
    @Size(max = 100, message = "合集名称不能超过100个字符")
    private String seriesName;

    @Size(max = 500, message = "合集简介不能超过500个字符")
    private String description;

    private Long coverFileId;

    @NotNull(message = "合集文章列表不能为空")
    @Size(max = 100, message = "单个合集最多支持100篇文章")
    private List<@Positive(message = "文章ID必须为正整数") Integer> articleIds = new ArrayList<>();
}
