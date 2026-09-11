package com.pblog.common.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Calendar;

@Service
@ConditionalOnExpression("'${storage.type:oss}' == 'oss' || '${storage.oss.enabled:false}' == 'true'")
public class AliyunOssStorageService implements ObjectStorageService {

    private final OSS ossClient;
    private final String bucketName;
    private final int urlExpireMinutes;

    public AliyunOssStorageService(
            OSS ossClient,
            @Value("${aliyun.oss.bucket-name}") String bucketName,
            @Value("${aliyun.oss.url-expire-minutes:30}") int urlExpireMinutes) {
        this.ossClient = ossClient;
        this.bucketName = bucketName;
        this.urlExpireMinutes = urlExpireMinutes;
    }

    @Override
    public void upload(String objectKey, byte[] content, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(content.length);
        if (contentType != null) {
            metadata.setContentType(contentType);
        }
        ossClient.putObject(bucketName, objectKey, new ByteArrayInputStream(content), metadata);
    }

    @Override
    public String getUrl(String objectKey) {
        String normalizedKey = normalizeKey(objectKey);
        Calendar expiration = Calendar.getInstance();
        expiration.add(Calendar.MINUTE, urlExpireMinutes);
        URL signedUrl = ossClient.generatePresignedUrl(bucketName, normalizedKey, expiration.getTime());
        return signedUrl.toString();
    }

    @Override
    public void delete(String objectKey) {
        ossClient.deleteObject(bucketName, normalizeKey(objectKey));
    }

    @Override
    public String getType() {
        return "oss";
    }

    private String normalizeKey(String objectKey) {
        if (objectKey == null || objectKey.trim().isEmpty()) {
            throw new IllegalArgumentException("图片文件名不能为空");
        }
        return objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
    }
}
