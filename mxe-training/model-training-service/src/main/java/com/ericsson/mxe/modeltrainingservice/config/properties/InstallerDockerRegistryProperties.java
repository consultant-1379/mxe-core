package com.ericsson.mxe.modeltrainingservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.PropertySource;
import jakarta.validation.constraints.NotNull;

@PropertySource("classpath:installer-docker-registry.properties")
@ConfigurationProperties(prefix = "installer-docker-registry")
public class InstallerDockerRegistryProperties {
    private final String caSecretName;

    public InstallerDockerRegistryProperties(@NotNull String caSecretName) {
        this.caSecretName = caSecretName;
    }

    public String getCaSecretName() {
        return caSecretName;
    }
}
