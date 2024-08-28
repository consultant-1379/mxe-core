package com.ericsson.mxe.modelcatalogueservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import jakarta.validation.constraints.NotEmpty;

@PropertySource("classpath:installer-docker-registry.properties")
@ConfigurationProperties(prefix = "installer-docker-registry")
public class InstallerDockerRegistryProperties {
    private final String caSecretName;

    public InstallerDockerRegistryProperties(@NotEmpty String caSecretName) {
        this.caSecretName = caSecretName;
    }

    public String getCaSecretName() {
        return caSecretName;
    }
}
