package com.ericsson.mxe.examples.restclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.DefaultOAuth2RequestAuthenticator;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    @Value("${mxe.client-id}")
    private String clientId;
    @Value("${mxe.access-token-uri}")
    private String accessTokenUri;
    @Value("${mxe.username}")
    private String username;
    @Value("${mxe.password}")
    private String password;

    @Bean
    public ResourceOwnerPasswordResourceDetails details() {
        ResourceOwnerPasswordResourceDetails cred = new ResourceOwnerPasswordResourceDetails();
        cred.setClientId(clientId);
        cred.setAccessTokenUri(accessTokenUri);
        cred.setUsername(username);
        cred.setPassword(password);
        return cred;
    }

    @Bean
    public OAuth2RestTemplate restTemplate(ResourceOwnerPasswordResourceDetails cred) {
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(cred);

        disableRedirects(restTemplate);
        setTemporaryRedirectErrorHandler(restTemplate);
        setupPasswordAuthentication(restTemplate, cred);

        return restTemplate;
    }

    /**
     * Sets up a {@link SimpleClientHttpRequestFactory} so that it will make connections that
     * would not follow redirect responses.
     *
     * This is necessary for MXE because when tokens are invalid or have expired the response will be 307!
     * This 307 response then must be handled as a token error!
     * @param restTemplate
     */
    private void disableRedirects(OAuth2RestTemplate restTemplate) {
        restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                connection.setInstanceFollowRedirects(false);
            }
        });
    }

    /**
     * MXE gatekeeper monitors all incoming requests and respond with 307 if an access token is invalid or expired.
     * This 307 response is mainly for the GUI authentication but API users have to manage it too.
     * @param restTemplate
     */
    private void setTemporaryRedirectErrorHandler(OAuth2RestTemplate restTemplate) {
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return response.getStatusCode().equals(HttpStatus.TEMPORARY_REDIRECT)
                        || super.hasError(response);
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if(response.getStatusCode().equals(HttpStatus.TEMPORARY_REDIRECT)) {
                    throw new OAuth2AccessDeniedException("Gatekeeper response 307, access token expired!");
                }
                super.handleError(response);
            }
        });
    }

    /**
     * Sets up {@link org.springframework.security.oauth2.client.token.AccessTokenProvider} to handle
     * requesting access token or refreshing it.
     *
     * Token refresh is only possible if {@link org.springframework.security.core.context.SecurityContext}
     * contains authentication object. Which needs to be manually set.
     *
     * @param restTemplate
     * @param cred
     */
    private void setupPasswordAuthentication(OAuth2RestTemplate restTemplate, ResourceOwnerPasswordResourceDetails cred) {
        ResourceOwnerPasswordAccessTokenProvider passwordGrantProvider = new ResourceOwnerPasswordAccessTokenProvider();
        AccessTokenProviderChain chain = new AccessTokenProviderChain(Collections.singletonList(passwordGrantProvider));
        restTemplate.setAccessTokenProvider(chain);

        // when authenticator is called a token has already been requested successfully so SecurityContext can be filled
        restTemplate.setAuthenticator(new DefaultOAuth2RequestAuthenticator() {
            @Override
            public void authenticate(OAuth2ProtectedResourceDetails res, OAuth2ClientContext ctx, ClientHttpRequest req) {
                super.authenticate(res, ctx, req);
                if (ctx.getAccessToken() != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(cred.getUsername(), cred.getPassword(), null));
                }
            }
        });
    }
}
