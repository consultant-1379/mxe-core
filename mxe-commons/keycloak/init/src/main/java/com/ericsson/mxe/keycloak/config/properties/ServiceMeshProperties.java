package com.ericsson.mxe.keycloak.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:service-mesh.properties")
@ConfigurationProperties(prefix = "service-mesh")
public class ServiceMeshProperties {
    private final Boolean mtlsEnabled;

    public ServiceMeshProperties(Boolean mtlsEnabled) {
        this.mtlsEnabled = mtlsEnabled;
    }

    public boolean isMtlsEnabled() {
        return Boolean.TRUE.equals(mtlsEnabled);
    }

}

