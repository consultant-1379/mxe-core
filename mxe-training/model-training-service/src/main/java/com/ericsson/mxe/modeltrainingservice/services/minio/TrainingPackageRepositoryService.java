package com.ericsson.mxe.modeltrainingservice.services.minio;

import java.io.InputStream;
import java.net.URL;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.ericsson.mxe.modeltrainingservice.exception.RepositoryErrorException;

@Service
public class TrainingPackageRepositoryService extends MinioRepositoryService implements MinioRepositoryUploader {
    private static final long MIN_BUFFER_MULTIPLIER = 10;

    public static final long MAX_OBJECT_SIZE = 5L * 1024 * 1024 * 1024 * 1024;
    // allowed minimum part size is 5MiB in multipart upload.
    public static final int MIN_MULTIPART_SIZE = 5 * 1024 * 1024;
    // allowed minimum part size is 5GiB in multipart upload.
    public static final long MAX_PART_SIZE = 5L * 1024 * 1024 * 1024;

    public TrainingPackageRepositoryService(final MinioBucketService trainingPackageRepository) {
        super(trainingPackageRepository);
    }

    public String putObject(final InputStream inputStream) {
        final String name = UUID.randomUUID().toString();
        final long partSize = Math.min(MIN_MULTIPART_SIZE * MIN_BUFFER_MULTIPLIER, MAX_PART_SIZE);

        try {
            getMinioBucketService().putObject(name, inputStream, partSize);
        } catch (Exception e) {
            throw new RepositoryErrorException(e);
        }

        return name;
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
