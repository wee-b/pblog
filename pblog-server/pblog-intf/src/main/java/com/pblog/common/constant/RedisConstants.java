package com.pblog.common.constant;

public class RedisConstants {

    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final String LOGIN_TOKEN_KEY = "login:token:";
    public static final String LOGIN_EmailCode_KEY = "login:email:code:";

    // 单位是秒
    public static final Integer CODE_EXPIRE = 120;


    /**
     * 构建用户点赞集合Key
     */
    public static String buildUserLikeKey(String targetType, Integer targetId) {
        return String.format("like:user:%s:%s", targetType, targetId);
    }

    /**
     * 构建点赞总数Key
     */
    public static String buildCountKey(String targetType, Integer targetId) {
        return String.format("like:count:%s:%s", targetType, targetId);
    }

    public static final Integer Like_Set_TTL = 60;  // 单位分钟

    /**
     * 点赞待同步集合Key（存储待同步的LikeDTO JSON）
     */
    public static final String LIKE_PENDING_SET = "like:pending";

    /**
     * 点赞消息唯一Key前缀（用于防抖判断）
     */
    public static final String LIKE_UNIQUE_KEY_PREFIX = "like:unique:";

    /**
     * 点赞同步延迟时间（分钟）
     */
    public static final int LIKE_DELAY_MINUTES = 3;

}
