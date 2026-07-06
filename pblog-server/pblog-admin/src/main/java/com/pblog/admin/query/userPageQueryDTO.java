package com.pblog.admin.query;

import com.pblog.common.domain.dto.PageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class userPageQueryDTO extends PageQueryDTO {

    /**
     * 用户状态(-1 表示查询全部)
     */
    @Size(max = 2, message = "参数错误")
    private String status;
}
