package com.ericsson.mxe.modeltrainingservice.services.minio;

public class MinioRepositoryService {
    private final MinioBucketService minioBucketService;

    public MinioRepositoryService(final MinioBucketService minioBucketService) {
        this.minioBucketService = minioBucketService;
    }

    protected MinioBucketService getMinioBucketService() {
        return minioBucketService;
    }
}
