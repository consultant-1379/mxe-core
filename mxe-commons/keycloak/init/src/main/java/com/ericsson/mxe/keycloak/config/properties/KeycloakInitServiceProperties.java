package com.ericsson.mxe.keycloak.config.properties;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak-init-service")
public class KeycloakInitServiceProperties {
    private final String username;
    private final String serverUrl;
    private final String password;

    public KeycloakInitServiceProperties(String username, String serverUrl, String password) {
        this.username = username;
        this.serverUrl = serverUrl;
        this.password = password;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
