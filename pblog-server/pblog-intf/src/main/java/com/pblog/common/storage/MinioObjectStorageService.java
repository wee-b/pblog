package com.pblog.common.storage;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnExpression("'${storage.type:oss}' == 'minio' || '${storage.minio.enabled:false}' == 'true'")
public class MinioObjectStorageService implements ObjectStorageService {

    private final MinioClient minioClient;
    private final String publicEndpoint;
    private final String bucketName;
    private final boolean publicRead;
    private final int urlExpireMinutes;

    public MinioObjectStorageService(
            MinioClient minioClient,
            @Value("${storage.minio.public-endpoint:${storage.minio.endpoint}}") String publicEndpoint,
            @Value("${storage.minio.bucket-name}") String bucketName,
            @Value("${storage.minio.public-read:true}") boolean publicRead,
            @Value("${storage.minio.url-expire-minutes:30}") int urlExpireMinutes) {
        this.minioClient = minioClient;
        this.publicEndpoint = trimTrailingSlash(publicEndpoint);
        this.bucketName = bucketName;
        this.publicRead = publicRead;
        this.urlExpireMinutes = urlExpireMinutes;
    }

    @PostConstruct
    public void initializeBucket() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
        if (publicRead) {
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(publicReadPolicy(bucketName))
                    .build());
        }
    }

    @Override
    public void upload(String objectKey, byte[] content, String contentType) {
        try {
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(normalizeKey(objectKey))
                    .stream(new ByteArrayInputStream(content), content.length, -1);
            if (contentType != null) {
                builder.contentType(contentType);
            }
            minioClient.putObject(builder.build());
        } catch (Exception e) {
            throw new IllegalStateException("MinIO 上传失败", e);
        }
    }

    @Override
    public String getUrl(String objectKey) {
        String normalizedKey = normalizeKey(objectKey);
        if (publicRead) {
            return publicEndpoint + "/" + bucketName + "/" + normalizedKey;
        }
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(normalizedKey)
                    .expiry(urlExpireMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("MinIO 访问地址生成失败", e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(normalizeKey(objectKey))
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("MinIO 删除失败", e);
        }
    }

    @Override
    public String getType() {
        return "minio";
    }

    private String normalizeKey(String objectKey) {
        if (objectKey == null || objectKey.trim().isEmpty()) {
            throw new IllegalArgumentException("图片文件名不能为空");
        }
        return objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String publicReadPolicy(String bucket) {
        return "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\","
                + "\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],"
                + "\"Resource\":[\"arn:aws:s3:::" + bucket + "/*\"]}]}";
    }
}
