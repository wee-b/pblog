package com.pblog.common.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("'${storage.type:oss}' == 'minio' || '${storage.minio.enabled:false}' == 'true'")
public class MinioConfig {

    @Bean
    public MinioClient minioClient(
            @Value("${storage.minio.endpoint}") String endpoint,
            @Value("${storage.minio.access-key}") String accessKey,
            @Value("${storage.minio.secret-key}") String secretKey) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
