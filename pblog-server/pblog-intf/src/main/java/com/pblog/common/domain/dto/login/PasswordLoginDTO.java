package com.pblog.common.domain.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordLoginDTO {
    // 账号或邮箱，保留 username 字段名以兼容现有客户端。
    private String username;
    private String password;
    // 图形验证码相关
    private String captchaUuid;
    private String captchaCode;
}
