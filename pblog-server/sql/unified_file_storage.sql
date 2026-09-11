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



