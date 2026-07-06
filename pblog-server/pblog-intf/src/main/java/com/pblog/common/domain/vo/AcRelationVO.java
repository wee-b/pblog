package com.pblog.common.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class AcRelationVO {

    private Integer articleId;
    private List<CategoryVO> categoryList;
}
