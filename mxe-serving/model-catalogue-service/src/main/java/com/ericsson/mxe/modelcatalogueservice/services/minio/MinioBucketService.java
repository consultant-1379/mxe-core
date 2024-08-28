package com.ericsson.mxe.modelcatalogueservice.services.minio;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ericsson.mxe.modelcatalogueservice.config.properties.MinioBucketProperties;
import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Item;

public class MinioBucketService {
    private final Logger logger = LoggerFactory.getLogger(MinioBucketService.class);

    protected final MinioClient minioClient;
    private final URL url;
    private final String bucket;
    private final String instanceSecretName;

    public MinioBucketService(final URL url, final MinioBucketProperties minioBucketProperties,
            final MinioClient.Factory minioClientFactory) throws Exception {
        this.url = url;
        this.bucket = minioBucketProperties.getBucket();
        this.instanceSecretName = minioBucketProperties.getInstanceSecretName();
        this.minioClient = minioClientFactory.create(url, minioBucketProperties.getServiceAccessKey(),
                minioBucketProperties.getServiceSecretKey());

        waitForBucket();
    }

    private void waitForBucket() throws InterruptedException {
        try {
            minioClient.bucketExists(bucket);
            while (!this.minioClient.bucketExists(bucket)) {
                logger.warn("Bucket " + bucket + " does not exist!");

                Thread.sleep(10000);
            }
        } catch (Exception e) {
            logger.warn("Cannot connect to Minio!");

            e.printStackTrace();

            Thread.sleep(10000);

            waitForBucket();
        }
    }

    public void putObject(String objectName, InputStream stream, long partSize) throws IOException, InvalidKeyException,
            NoSuchAlgorithmException, IllegalArgumentException, MinioException {
        this.minioClient.putObject(bucket, objectName, stream, partSize);
    }

    public void removeObject(String objectName) throws IOException, InvalidKeyException, NoSuchAlgorithmException,
            IllegalArgumentException, MinioException {
        this.minioClient.removeObject(bucket, objectName);
    }

    public InputStream getObject(String objectName)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, InternalException, XmlParserException, ErrorResponseException, MinioException {
        return this.minioClient.getObject(bucket, objectName);
    }

    public boolean bucketExists() throws ErrorResponseException, IllegalArgumentException, InsufficientDataException,
            InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException,
            XmlParserException, MinioException {
        return this.minioClient.bucketExists(bucket);
    }

    public StatObjectResponse statObject(String objectName) throws IllegalArgumentException, IOException,
            NoSuchAlgorithmException, MinioException, InvalidKeyException {
        return this.minioClient.statObject(objectName, objectName);
    }

    public Iterable<Result<Item>> listObjects() throws XmlParserException {
        return this.minioClient.listObjects(bucket);
    }

    public String getInstanceSecretName() {
        return this.instanceSecretName;
    }

    public URL getUrl() {
        return url;
    }

    public String getBucket() {
        return bucket;
    }
}
