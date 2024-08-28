package com.ericsson.mxe.modelcatalogueservice.services.minio;

import java.io.InputStream;
import java.net.URL;

public interface MinioRepositoryUploader {
    String putObject(final InputStream inputStream);

    URL getUrl();

    String getBucket();

    String getInstanceSecretName();

    void removeObject(final String objectName);
}
