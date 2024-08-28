package com.ericsson.mxe.modeltrainingservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:service-mesh.properties")
@ConfigurationProperties(prefix = "service-mesh")
public class ServiceMeshProperties {
    private final Boolean mtlsEnabled;
    private final String userVolume;
    private final String userVolumeMounts;

    public ServiceMeshProperties(Boolean mtlsEnabled, String userVolume, String userVolumeMounts) {
        this.mtlsEnabled = mtlsEnabled;
        this.userVolume = userVolume;
        this.userVolumeMounts = userVolumeMounts;
    }

    public boolean isMtlsEnabled() {
        return Boolean.TRUE.equals(mtlsEnabled);
    }

    public String getUserVolume() {
        return userVolume;
    }

    public String getUserVolumeMounts() {
        return userVolumeMounts;
    }
}
