package com.pblog.common.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class UserInfoVO {

    public String username;
    public String nickname;
    public String avatarUrl;
    @JsonIgnore
    public String minioUrl;
    @JsonIgnore
    public Long avatarFileId;
    public String bio;
}
