package com.ericsson.mxe.modelcatalogueservice.config;

import java.net.URL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.ericsson.mxe.modelcatalogueservice.config.properties.MinioBucketProperties;
import com.ericsson.mxe.modelcatalogueservice.services.minio.MinioClient;
import com.ericsson.mxe.modelcatalogueservice.services.minio.TestMinioBucketService;

@Configuration
@Profile("test")
public class TestModelSourceRepositoryConfig {
    @Bean
    public TestMinioBucketService modelSourceRepository(final MinioClient.Factory minioClientFactory) throws Exception {
        return new TestMinioBucketService(new URL("http://test:9000"),
                new MinioBucketProperties(null, null, null, null), minioClientFactory);
    }
}
