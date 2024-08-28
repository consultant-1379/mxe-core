package com.ericsson.mxe.modelcatalogueservice.config;

import com.ericsson.mxe.modelcatalogueservice.services.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("production")
public class MinioConfig {
    @Bean
    public MinioClient.Factory minioClientFactory() {
        return new MinioClient.Factory();
    }
}
