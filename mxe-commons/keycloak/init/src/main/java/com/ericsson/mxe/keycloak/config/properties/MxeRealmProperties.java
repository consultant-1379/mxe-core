package com.ericsson.mxe.keycloak.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "realm")
public class MxeRealmProperties {
    private final String name;
    private final String redirectUrl;
    private final String username;
    private final String password;
    private final boolean temporalUser;

    public MxeRealmProperties(String name, String redirectUrl, String username, String password, boolean temporalUser) {
        this.name = name;
        this.redirectUrl = redirectUrl;
        this.username = username;
        this.password = password;
        this.temporalUser = temporalUser;
    }

    public String getName() {
        return name;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isTemporalUser() {
        return temporalUser;
    }
}
