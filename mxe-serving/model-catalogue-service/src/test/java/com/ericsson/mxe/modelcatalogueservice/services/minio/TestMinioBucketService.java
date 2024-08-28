package com.ericsson.mxe.modelcatalogueservice.services.minio;

import java.net.URL;
import com.ericsson.mxe.modelcatalogueservice.config.properties.MinioBucketProperties;

public class TestMinioBucketService extends MinioBucketService {
    public TestMinioBucketService(URL url, MinioBucketProperties minioBucketProperties,
            MinioClient.Factory minioClientFactory) throws Exception {
        super(url, minioBucketProperties, minioClientFactory);
    }

    public MinioClient getClient() {
        return this.minioClient;
    }
}
