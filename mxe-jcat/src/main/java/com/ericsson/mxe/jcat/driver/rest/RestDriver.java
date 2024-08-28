package com.ericsson.mxe.jcat.driver.rest;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;

public class RestDriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestDriver.class);
    private static final String LOGIN_FORM =
            "username=%s&password=%s&grant_type=password&client_id=mxe-rest-client&scope=offline_access";
    private static final String REFRESH_FORM =
            "grant_type=refresh_token&client_id=mxe-rest-client&scope=offline_access&refresh_token=%s";
    private static final String AUTHENTICATION_ENDPOINT = "auth/realms/mxe/protocol/openid-connect/token";
    private static final String REFRESH_NOT_NECESSARY = "Refresh not necessary.";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String EXPIRES_IN = "expires_in";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String CONNECTION_FAILED = "Connection failed";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final int DEFAULT_REQUEST_TIMEOUT_MILLIS = 5000;

    private final RestTemplate restTemplate;
    private final String url;
    private final String user;
    private final String password;
    private String accessToken;
    private int expiresIn;
    private String refreshToken;
    private long tokenTime;

    public RestDriver(final String url, final String user, final String password) {
        this(url, user, password, DEFAULT_REQUEST_TIMEOUT_MILLIS);
    }

    public RestDriver(final String url, final String user, final String password, final int requestTimeout) {
        restTemplate = createRestTemplate(requestTimeout);
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public ResponseEntity<String> login() {
        ResponseEntity<String> response =
                restTemplate.exchange(RestHelper.link(url, AUTHENTICATION_ENDPOINT), HttpMethod.POST,
                        new HttpEntity<>(String.format(LOGIN_FORM, user, password), createLoginHeader()), String.class);
        handleLogin(response);
        return response;
    }

    public ResponseEntity<String> refresh() {
        ResponseEntity<String> response =
                restTemplate.exchange(RestHelper.link(url, AUTHENTICATION_ENDPOINT), HttpMethod.POST,
                        new HttpEntity<>(String.format(REFRESH_FORM, refreshToken), createLoginHeader()), String.class);
        handleLogin(response);
        return response;
    }

    private RestTemplate createRestTemplate(final int requestTimeout) {
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext =
                    org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setConnectTimeout(requestTimeout);
            requestFactory.setReadTimeout(requestTimeout);
            requestFactory.setHttpClient(httpClient);
            return new RestTemplate(requestFactory);
        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            LOGGER.error(CONNECTION_FAILED, e);
            throw new RuntimeException(CONNECTION_FAILED, e);
        }
    }

    public <T> ResponseEntity<T> send(Request<T> request) {
        return send(request.getEndPoint(), request.getMethod(), request.getData(), request.getHttpHeaders(),
                request.getResponseType());
    }

    public <T> ResponseEntity<T> send(final String endPoint, final HttpMethod method, Object data,
            HttpHeaders httpHeaders, Class<T> clazz) {
        refreshIfNecessary();
        return restTemplate.exchange(RestHelper.link(url, endPoint), method,
                new HttpEntity<>(data, createTokenHeader(httpHeaders)), clazz);
    }

    private void refreshIfNecessary() {
        if (System.currentTimeMillis() > tokenTime + expiresIn - 500) {
            refresh();
        } else {
            LOGGER.debug(REFRESH_NOT_NECESSARY);
        }
    }

    private void handleLogin(ResponseEntity<String> loginResponse) {
        Map<String, String> loginMap = RestHelper.getMapFromString(loginResponse.getBody());
        accessToken = loginMap.get(ACCESS_TOKEN);
        expiresIn = Integer.parseInt(loginMap.get(EXPIRES_IN)) * 1000;
        refreshToken = loginMap.get(REFRESH_TOKEN);
        tokenTime = System.currentTimeMillis();
    }

    private HttpHeaders createLoginHeader() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return httpHeaders;
    }

    private HttpHeaders createTokenHeader(HttpHeaders httpHeaders) {
        httpHeaders.set(AUTHORIZATION, BEARER + accessToken);
        return httpHeaders;
    }
}
