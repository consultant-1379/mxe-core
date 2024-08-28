package com.ericsson.mxe.modeltrainingservice.config;

import java.net.URL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.ericsson.mxe.modeltrainingservice.config.properties.MinioBucketProperties;
import com.ericsson.mxe.modeltrainingservice.services.minio.MinioClient;
import com.ericsson.mxe.modeltrainingservice.services.minio.TestMinioBucketService;

@Configuration
@Profile("test")
public class TestTrainingJobResultRepositoryConfig {
    @Bean
    public TestMinioBucketService trainingJobResultRepository(final MinioClient.Factory minioClientFactory)
            throws Exception {
        return new TestMinioBucketService(new URL("http://test:9000"),
                new MinioBucketProperties(null, null, null, null), minioClientFactory);
    }
}
