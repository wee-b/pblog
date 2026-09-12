-- Step 1: create the unified file table and add file ID reference columns.
CREATE TABLE IF NOT EXISTS `pb_file` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `provider` varchar(16) NOT NULL COMMENT 'oss or minio',
    `object_key` varchar(512) NOT NULL,
    `original_name` varchar(255) DEFAULT NULL,
    `content_type` varchar(128) DEFAULT NULL,
    `file_size` bigint NOT NULL DEFAULT 0,
    `file_type` varchar(32) NOT NULL,
    `status` char(1) NOT NULL DEFAULT '1',
    `create_by` int DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_provider_object_key` (`provider`, `object_key`),
    KEY `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Unified file metadata';

ALTER TABLE `pb_user`
    ADD COLUMN `avatar_file_id` bigint DEFAULT NULL,
    ADD KEY `idx_avatar_file_id` (`avatar_file_id`);

ALTER TABLE `pb_article`
    ADD COLUMN `cover_file_id` bigint DEFAULT NULL,
    ADD KEY `idx_cover_file_id` (`cover_file_id`);

ALTER TABLE `pb_series`
    ADD COLUMN `cover_file_id` bigint DEFAULT NULL,
    ADD KEY `idx_series_cover_file_id` (`cover_file_id`);
