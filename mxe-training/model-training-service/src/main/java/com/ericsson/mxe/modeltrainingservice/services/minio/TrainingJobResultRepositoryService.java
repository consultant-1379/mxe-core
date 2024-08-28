package com.ericsson.mxe.modeltrainingservice.services.minio;

import java.io.InputStream;
import java.net.URL;
import org.springframework.stereotype.Service;
import com.ericsson.mxe.modeltrainingservice.exception.RepositoryErrorException;

@Service
public class TrainingJobResultRepositoryService extends MinioRepositoryService {
    public TrainingJobResultRepositoryService(final MinioBucketService trainingJobResultRepository) {
        super(trainingJobResultRepository);
    }

    public InputStream getObject(final String name) {
        try {
            return getMinioBucketService().getObject(name);
        } catch (Exception e) {
            throw new RepositoryErrorException(e);
        }
    }

    public void removeObject(final String name) {
        try {
            getMinioBucketService().removeObject(name);
        } catch (Exception e) {
            throw new RepositoryErrorException(e);
        }
    }

    public URL getUrl() {
        return getMinioBucketService().getUrl();
    }

    public String getBucket() {
        return getMinioBucketService().getBucket();
    }

    public String getInstanceSecretName() {
        return getMinioBucketService().getInstanceSecretName();
    }
}
