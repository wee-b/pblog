package com.pblog.common.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAdminInfoVO {

    public String username;
    public String nickname;
    public String avatarUrl;
    @JsonIgnore
    public Long avatarFileId;
    public String email;


    public LocalDateTime lastLoginTime;
    public LocalDateTime createTime;
    public String status;
    public String remark;
}

