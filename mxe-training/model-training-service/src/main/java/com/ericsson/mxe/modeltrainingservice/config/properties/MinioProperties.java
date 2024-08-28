package com.ericsson.mxe.modeltrainingservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:minio.properties")
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    private final String url;
    private final MinioBucketProperties trainingPackage;
    private final MinioBucketProperties trainingJobResult;

    public MinioProperties(String url, MinioBucketProperties trainingPackage, MinioBucketProperties trainingJobResult) {
        this.url = url;
        this.trainingPackage = trainingPackage;
        this.trainingJobResult = trainingJobResult;
    }

    public String getUrl() {
        return url;
    }

    public MinioBucketProperties getTrainingPackage() {
        return trainingPackage;
    }

    public MinioBucketProperties getTrainingJobResult() {
        return trainingJobResult;
    }
}
