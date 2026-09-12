-- Execute after unified_file_storage.sql and after confirming file ID references are ready.
ALTER TABLE `pb_user`
    DROP COLUMN `avatar_url`,
    DROP COLUMN `minio_url`;

ALTER TABLE `pb_article`
    DROP COLUMN `cover_image`;

ALTER TABLE `pb_series`
    DROP COLUMN `cover_image`;
