package com.ericsson.mxe.modelcatalogueservice.services.minio;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Item;

public class MinioClient {
    private final io.minio.MinioClient implementation;

    private MinioClient(final URL url, final String accessKey, final String secretKey) throws Exception {

        implementation = io.minio.MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();
    }

    public boolean bucketExists(String bucketName) throws ErrorResponseException, IllegalArgumentException,
            InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException,
            NoSuchAlgorithmException, XmlParserException, MinioException {
        return implementation.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }


    public StatObjectResponse statObject(String bucketName, String objectName) throws ErrorResponseException,
            IllegalArgumentException, InsufficientDataException, InternalException, InvalidKeyException,
            InvalidResponseException, IOException, NoSuchAlgorithmException, XmlParserException, MinioException {

        return implementation.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    public void putObject(String bucketName, String objectName, InputStream stream, long partSize)
            throws ErrorResponseException, IllegalArgumentException, InsufficientDataException, InternalException,
            InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, XmlParserException,
            MinioException {
        implementation.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(stream, -1, partSize).build());

    }

    public void removeObject(String bucketName, String objectName) throws ErrorResponseException,
            IllegalArgumentException, InsufficientDataException, InternalException, InvalidKeyException,
            InvalidResponseException, IOException, NoSuchAlgorithmException, XmlParserException, MinioException {
        implementation.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    public Iterable<Result<Item>> listObjects(final String bucketName) throws XmlParserException {
        return implementation.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
    }

    public InputStream getObject(String bucketName, String objectName) throws ErrorResponseException,
            IllegalArgumentException, InsufficientDataException, InternalException, InvalidKeyException,
            InvalidResponseException, IOException, NoSuchAlgorithmException, XmlParserException, MinioException {

        return implementation.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());

    }

    public static class Factory {
        public MinioClient create(final URL url, final String accessKey, final String secretKey) throws Exception {
            return new MinioClient(url, accessKey, secretKey);
        }
    }
}
