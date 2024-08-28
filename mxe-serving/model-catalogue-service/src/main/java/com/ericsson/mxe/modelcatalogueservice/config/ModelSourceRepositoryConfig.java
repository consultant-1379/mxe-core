package com.ericsson.mxe.modelcatalogueservice.config;

import java.net.URL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.ericsson.mxe.modelcatalogueservice.config.properties.MinioProperties;
import com.ericsson.mxe.modelcatalogueservice.services.minio.MinioBucketService;
import com.ericsson.mxe.modelcatalogueservice.services.minio.MinioClient;

@Configuration
@Profile("production")
public class ModelSourceRepositoryConfig {
    @Bean
    public MinioBucketService modelSourceRepository(final MinioProperties minioProperties,
            final MinioClient.Factory minioClientFactory) throws Exception {
        return new MinioBucketService(new URL(minioProperties.getUrl()), minioProperties.getModelSource(),
                minioClientFactory);
    }
}
