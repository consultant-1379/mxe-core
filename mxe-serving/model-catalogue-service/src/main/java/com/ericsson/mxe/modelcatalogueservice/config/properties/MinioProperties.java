package com.ericsson.mxe.modelcatalogueservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:minio.properties")
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    private final String url;
    private final MinioBucketProperties modelSource;

    public MinioProperties(String url, MinioBucketProperties modelSource) {
        this.url = url;
        this.modelSource = modelSource;
    }

    public String getUrl() {
        return url;
    }

    public MinioBucketProperties getModelSource() {
        return modelSource;
    }

}
