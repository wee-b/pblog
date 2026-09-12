-- 为已有数据库补充合集封面字段。
-- 全新数据库请直接使用“一键搭建mysql.sql”，无需重复执行本脚本。
ALTER TABLE `pb_series`
    ADD COLUMN `cover_file_id` bigint DEFAULT NULL COMMENT '系列封面文件ID' AFTER `series_name`,
    ADD KEY `idx_series_cover_file_id` (`cover_file_id`);
