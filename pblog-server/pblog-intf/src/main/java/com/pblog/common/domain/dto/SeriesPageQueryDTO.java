package com.pblog.common.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SeriesPageQueryDTO extends PageQueryDTO {

    @Size(max = 50, message = "搜索关键词不能超过50个字符")
    private String keyword;
}
