package com.pblog.user.module.business.like.service;

import com.pblog.common.domain.dto.LikeDTO;

public interface LikeService {
    void handleLikeRequest(LikeDTO dto);

    boolean isLiked(String targetType, Integer targetId);

    Integer getLikeCount(String targetType, Integer targetId);

}
