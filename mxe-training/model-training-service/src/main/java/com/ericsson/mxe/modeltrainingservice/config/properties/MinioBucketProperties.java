package com.ericsson.mxe.modeltrainingservice.config.properties;

public class MinioBucketProperties {
    private final String serviceAccessKey;
    private final String serviceSecretKey;
    private final String bucket;
    private final String instanceSecretName;

    public MinioBucketProperties(String serviceAccessKey, String serviceSecretKey, String bucket,
            String instanceSecretName) {
        this.serviceAccessKey = serviceAccessKey;
        this.serviceSecretKey = serviceSecretKey;
        this.bucket = bucket;
        this.instanceSecretName = instanceSecretName;
    }

    public String getServiceAccessKey() {
        return serviceAccessKey;
    }

    public String getServiceSecretKey() {
        return serviceSecretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public String getInstanceSecretName() {
        return instanceSecretName;
    }
}
