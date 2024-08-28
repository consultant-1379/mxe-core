package com.ericsson.mxe.jcat.driver.rest;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class Request<T> {

    private static final String DATA = "data";
    private String endPoint;
    private HttpMethod method;
    private Object data;
    private HttpHeaders httpHeaders = new HttpHeaders();
    private Class<T> clazz;

    public static <T> Request<T> create(String endPoint, HttpMethod method, Class<T> clazz) {
        return createRequest(endPoint, method, null, clazz);
    }

    public static <T> Request<T> create(String endPoint, HttpMethod method, Class<T> clazz, String data) {
        Request<T> request = createRequest(endPoint, method, data, clazz);
        request.httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return request;
    }

    public static <T> Request<T> create(String endPoint, HttpMethod method, Class<T> clazz, String fileKey,
            FileSystemResource data) {
        return create(endPoint, method, clazz, fileKey, data, null);
    }

    public static <T> Request<T> create(String endPoint, HttpMethod method, Class<T> clazz, String fileKey,
            FileSystemResource data, String additionalData) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        if (additionalData != null) {
            body.add(DATA, additionalData);
        }
        body.add(fileKey, data);
        Request<T> request = createRequest(endPoint, method, body, clazz);
        request.httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        return request;
    }

    private static <T> Request<T> createRequest(String endPoint, HttpMethod method, Object data, Class<T> clazz) {
        Request<T> request = new Request<>();
        request.endPoint = endPoint;
        request.method = method;
        request.data = data;
        request.clazz = clazz;
        return request;
    }

    private Request() {}

    public Class<T> getResponseType() {
        return clazz;
    }

    public Object getData() {
        return data;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }
}
