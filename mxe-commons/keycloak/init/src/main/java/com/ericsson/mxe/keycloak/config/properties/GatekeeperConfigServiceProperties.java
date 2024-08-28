package com.ericsson.mxe.keycloak.config.properties;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gatekeeper-config-service")
public class GatekeeperConfigServiceProperties {
    private final String configSecretName;

    public GatekeeperConfigServiceProperties(String configSecretName) {
        this.configSecretName = configSecretName;
    }

    public String getConfigSecretName() {
        return configSecretName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
