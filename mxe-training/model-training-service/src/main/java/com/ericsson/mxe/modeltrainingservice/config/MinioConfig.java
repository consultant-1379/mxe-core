package com.ericsson.mxe.modeltrainingservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.ericsson.mxe.modeltrainingservice.services.minio.MinioClient;

@Configuration
@Profile("production")
public class MinioConfig {
    @Bean
    public MinioClient.Factory minioClientFactory() {
        return new MinioClient.Factory();
    }
}
