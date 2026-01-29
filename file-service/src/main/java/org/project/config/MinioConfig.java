package org.project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioConfig {
    
    private String endpoint;
    private String publicEndpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    @Bean(name = "minioClient")
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean(name = "publicMinioClient")
    public MinioClient publicMinioClient() {
        String urlEndpoint = (publicEndpoint != null && !publicEndpoint.isEmpty())
                ? publicEndpoint 
                : endpoint;
        return MinioClient.builder()
                .endpoint(urlEndpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}