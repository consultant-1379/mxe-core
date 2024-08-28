package com.ericsson.mxe.modelcatalogueservice.config;

import com.ericsson.mxe.modelcatalogueservice.services.minio.MinioClient;
import io.minio.errors.*;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Configuration
@Profile("test")
public class TestMinioConfig {
    public static class TestMinioClientFactory extends MinioClient.Factory {
        @Override
        public MinioClient create(URL url, String accessKey, String secretKey) {
            final MinioClient minioClient = Mockito.mock(MinioClient.class);

            try {
                when(minioClient.bucketExists(any())).thenReturn(true);
            } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException
                    | IllegalArgumentException e) {
                e.printStackTrace();
            }

            return minioClient;
        }
    }

    @Bean
    public TestMinioClientFactory minioClientFactory() {
        return new TestMinioClientFactory();
    }
}
