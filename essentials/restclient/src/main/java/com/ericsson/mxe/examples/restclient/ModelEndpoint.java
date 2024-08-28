package com.ericsson.mxe.examples.restclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;

@Service
public class ModelEndpoint {

    private static final Logger logger = LogManager.getLogger(ModelEndpoint.class);

    @Value("${mxe.model-service-uri}")
    private String mxeModelServiceUri;

    @Autowired
    private OAuth2RestTemplate restTemplate;

    public void send(byte[] data) {
        String result = restTemplate.exchange(
                mxeModelServiceUri,
                HttpMethod.POST,
                new HttpEntity<>(new ModelInput(data)),
                String.class).getBody();
        logger.info(result);
    }
}
