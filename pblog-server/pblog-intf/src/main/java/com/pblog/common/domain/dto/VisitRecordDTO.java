package com.pblog.common.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VisitRecordDTO {

    @NotBlank(message = "设备指纹不能为空")
    @Size(max = 2048, message = "设备指纹长度不能超过2048个字符")
    private String fingerprint;
}
