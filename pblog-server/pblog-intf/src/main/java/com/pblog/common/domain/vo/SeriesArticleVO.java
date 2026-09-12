package com.pblog.common.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SeriesArticleVO extends ArticleVO {
    private Integer sortOrder;
}
