/*
SQLyog v10.2
MySQL - 5.5.40 : Database - pblog
*********************************************************************
*/

-- 创建数据库（若不存在），指定字符集为utf8mb4
CREATE DATABASE /*!32312 IF NOT EXISTS*/`pblog` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;
USE `pblog`;
/*!40101 SET NAMES utf8mb4 */;
/*!40101 SET SQL_MODE=''*/;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- 一、权限相关表
-- 1.1 菜单权限表（id改为int，字符集utf8mb4）
DROP TABLE IF EXISTS `pb_menu`;
CREATE TABLE `pb_menu` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
    `parent_id` int DEFAULT '0' COMMENT '父菜单ID',
    `order_num` int(4) DEFAULT '0' COMMENT '显示顺序',
    `path` varchar(200) DEFAULT '' COMMENT '路由地址',
    `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
    `is_frame` int(1) DEFAULT '1' COMMENT '是否为外链（0是 1否）',
    `menu_type` char(1) DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
    `visible` char(1) DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
    `status` char(1) DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
    `perms` varchar(100) DEFAULT NULL COMMENT '权限标识（如content:article:list）',
    `icon` varchar(100) DEFAULT '#' COMMENT '菜单图标',
    `create_by` int DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` int DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT '' COMMENT '备注',
    `del_flag` char(1) DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2034 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单权限表';



-- 1.2 角色表（id改为int，字符集utf8mb4）
DROP TABLE IF EXISTS `pb_role`;
CREATE TABLE `pb_role` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name` varchar(30) NOT NULL COMMENT '角色名称',
    `role_key` varchar(100) NOT NULL COMMENT '角色权限字符串',
    `role_sort` int(4) NOT NULL COMMENT '显示顺序',
    `status` char(1) NOT NULL COMMENT '角色状态（0正常 1停用）',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
    `create_by` int DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` int DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色信息表';




-- 1.3 角色菜单关联表（外键改为int，字符集utf8mb4）
DROP TABLE IF EXISTS `pb_role_menu`;
CREATE TABLE `pb_role_menu` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id` int NOT NULL COMMENT '角色ID',
    `menu_id` int NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`) COMMENT '确保角色与菜单的关联唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色和菜单关联表';



-- 1.4 用户角色关联表（外键改为int，字符集utf8mb4）
DROP TABLE IF EXISTS `pb_user_role`;
CREATE TABLE `pb_user_role` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` int NOT NULL COMMENT '用户ID',
    `role_id` int NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`,`role_id`) COMMENT '确保用户与角色的关联唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户和角色关联表';




-- 二、博客业务核心表
-- 2.1 用户表（id改为int，字符集utf8mb4）
DROP TABLE IF EXISTS `pb_user`;
CREATE TABLE `pb_user` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键',
    `username` varchar(50) NOT NULL COMMENT '用户名（登录账号）',
    `password` varchar(100) NOT NULL COMMENT '密码（加密存储）',
    `nickname` varchar(50) DEFAULT NULL COMMENT '用户昵称',
    `email` varchar(100) DEFAULT NULL COMMENT '用户邮箱',
    `avatar_url` varchar(255) DEFAULT NULL COMMENT '用户头像路径',
    `status` char(1) NOT NULL DEFAULT '1' COMMENT '账号状态（0正常 1禁用）',
    `bio` varchar(500) DEFAULT '' COMMENT '用户简介',
    `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `create_by` int DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` int DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` varchar(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status_del_flag` (`status`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='博客用户表';

-- 2.2 文章表（id改为int，字符集utf8mb4）
DROP TABLE IF EXISTS `pb_article`;
CREATE TABLE `pb_article` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '文章ID，主键',
    `title` varchar(200) NOT NULL COMMENT '文章标题',
    `content` longtext NOT NULL COMMENT '文章正文',
    `summary` varchar(500) DEFAULT '' COMMENT '文章摘要',
    `cover_image` varchar(255) DEFAULT NULL COMMENT '文章封面URL',
    `author_username` varchar(50) NOT NULL COMMENT '作者账号（关联pb_user）',
    `author_nickname` varchar(50) NOT NULL COMMENT '作者昵称',
    `status` char(1) NOT NULL DEFAULT '0' COMMENT '文章状态 : 0草稿 1已发布 2待审核',
    `view_count` int(11) NOT NULL DEFAULT '0' COMMENT '阅读量',
    `like_count` int(11) NOT NULL DEFAULT '0' COMMENT '点赞数',
    `comment_count` int(11) NOT NULL DEFAULT '0' COMMENT '评论数',
    `sticky` char(1) NOT NULL DEFAULT '0' COMMENT '是否置顶（0否 1是）',
    `featured` char(1) NOT NULL DEFAULT '0' COMMENT '是否推荐（0否 1是）',
    `published_at` datetime DEFAULT NULL COMMENT '发布时间',
    `category_names` varchar(100) DEFAULT NULL COMMENT '标签名称，用:分隔',


    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `create_by` int DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` int DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` varchar(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_author_id_status` (`author_username`,`status`),
    KEY `idx_published_at` (`published_at`),
    KEY `idx_title` (`title`(50))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='博客文章表';

-- 2.3 分类表（id改为int，字符集utf8mb4）
DROP TABLE IF EXISTS `pb_category`;
CREATE TABLE `pb_category` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID，主键',
    `category_name` varchar(50) NOT NULL COMMENT '分类名称',
    `parent_id` int DEFAULT '0' COMMENT '父分类ID（0为一级分类）',
    `order_num` int(4) DEFAULT '0' COMMENT '显示顺序',
    `description` varchar(500) COMMENT '描述',
    `create_by` int DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` int DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` varchar(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章分类表';

-- 2.4 文章-分类关联表（外键改为int，字符集utf8mb4）
DROP TABLE IF EXISTS `pb_ac_relation`;
CREATE TABLE `pb_ac_relation` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '关联ID，主键',
    `article_id` int NOT NULL COMMENT '文章ID（关联pb_article）',
    `category_id` int NOT NULL COMMENT '分类ID（关联pb_category）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_category` (`article_id`,`category_id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章与分类关联表';

-- 2.5 系列表（原合集表）
DROP TABLE IF EXISTS `pb_series`;
CREATE TABLE `pb_series` (
                             `id` int NOT NULL AUTO_INCREMENT COMMENT '系列ID，主键',
                             `series_name` varchar(100) NOT NULL COMMENT '系列名称',
                             `cover_image` varchar(255) DEFAULT NULL COMMENT '系列封面URL',
                             `description` varchar(500) DEFAULT '' COMMENT '系列简介',
                             `status` char(1) NOT NULL DEFAULT '0' COMMENT '系列状态（0正常 1禁用）',
                             `author_username` varchar(50) NOT NULL COMMENT '创建者账号（关联pb_user）',
                             `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
                             `create_by` int DEFAULT NULL COMMENT '创建者',
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_by` int DEFAULT NULL COMMENT '更新者',
                             `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_series_name_author` (`series_name`,`author_username`),
                             KEY `idx_author_id_status` (`author_username`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章系列表';

-- 2.6 系列-文章关联表
DROP TABLE IF EXISTS `pb_sa_relation`;
CREATE TABLE `pb_sa_relation` (
                                  `id` int NOT NULL AUTO_INCREMENT COMMENT '关联ID，主键',
                                  `series_id` int NOT NULL COMMENT '系列ID（关联pb_series）',
                                  `article_id` int NOT NULL COMMENT '文章ID（关联pb_article）',
                                  `sort_order` int(4) DEFAULT '0' COMMENT '排序顺序（值越小越靠前）',
                                  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
                                  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_series_article` (`series_id`,`article_id`),
                                  KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系列与文章关联表';

-- 2.7 评论表/留言板
DROP TABLE IF EXISTS `pb_comment`;
CREATE TABLE `pb_comment` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '评论ID，主键',
    `article_id` int NOT NULL COMMENT '文章ID（关联pb_article）文章id=0代表为留言板数据',
    `username` int NOT NULL COMMENT '评论用户账号(10位)',
    `root_id`int NOT NULL COMMENT '评论根目录 0为根目录',
    `parent_id` int DEFAULT NULL COMMENT '父评论ID，0为根目录，最多支持两级评论',
    `to_reply_username` int DEFAULT NULL COMMENT '被回复人账号(10位)',
    `content` varchar(1000) NOT NULL COMMENT '评论内容',
    `status` char(1) NOT NULL DEFAULT '0' COMMENT '评论状态（0待审核 1已通过 2已驳回）',
    `like_count` int(11) NOT NULL DEFAULT '0' COMMENT '点赞数',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_id_status` (`article_id`,`status`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章评论表';

-- 2.8 点赞表
DROP TABLE IF EXISTS `pb_like`;
CREATE TABLE `pb_like` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '点赞ID，主键',
    `username` varchar(50) NOT NULL COMMENT '用户账号（关联pb_user）',
    `target_id` int unsigned NOT NULL COMMENT '内容ID',
    `target_type` varchar(2) NOT NULL COMMENT '内容类型other(0)/article(1)/comment(2)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    -- 新增唯一索引：确保一个用户对同一个内容只能点赞一次（防止脏数据）
    UNIQUE KEY `uk_user_target` (`username`, `target_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='点赞表';

-- 2.9 点赞总数表
DROP TABLE IF EXISTS `pb_like_counts`;
CREATE TABLE `pb_like_counts` (
   `target_id` int unsigned NOT NULL COMMENT '内容ID',
   `target_type` varchar(2) NOT NULL COMMENT '内容类型other(0)/article(1)/comment(2)',
   `total` int unsigned NOT NULL DEFAULT 0 COMMENT '点赞总数',
   PRIMARY KEY (`target_id`, `target_type`), -- 组合主键替代单独ID，更精简
   KEY `idx_total` (`total`) -- 优化按点赞数排序查询
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞总数统计表';


-- 2.10 友链表
DROP TABLE IF EXISTS `pb_friend_link`;
CREATE TABLE `pb_friend_link` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '友链ID，主键',
    `site_name` varchar(100) NOT NULL COMMENT '网站名称',
    `site_url` varchar(255) NOT NULL COMMENT '网站URL',
    `logo` varchar(255) DEFAULT NULL COMMENT '网站LOGO',
    `description` varchar(500) DEFAULT '' COMMENT '网站简介',
    `contact_email` varchar(100) DEFAULT NULL COMMENT '联系人邮箱',
    `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0待审核 1已通过 2已拒绝）',
    `sort_order` int(4) DEFAULT '0' COMMENT '显示顺序',
    `approved_by` int DEFAULT NULL COMMENT '审核人ID（关联pb_user）',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    `create_by` int DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_site_url` (`site_url`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='友情链接表';

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;