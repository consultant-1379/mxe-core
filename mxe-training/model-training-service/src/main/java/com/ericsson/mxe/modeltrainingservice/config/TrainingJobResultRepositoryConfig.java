package com.ericsson.mxe.modeltrainingservice.config;

import java.net.URL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.ericsson.mxe.modeltrainingservice.config.properties.MinioProperties;
import com.ericsson.mxe.modeltrainingservice.services.minio.MinioBucketService;
import com.ericsson.mxe.modeltrainingservice.services.minio.MinioClient;

@Configuration
@Profile("production")
public class TrainingJobResultRepositoryConfig {
    @Bean
    public MinioBucketService trainingJobResultRepository(final MinioProperties minioProperties,
            final MinioClient.Factory minioClientFactory) throws Exception {
        return new MinioBucketService(new URL(minioProperties.getUrl()), minioProperties.getTrainingJobResult(),
                minioClientFactory);
    }
}
