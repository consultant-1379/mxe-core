package com.ericsson.mxe.modelservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:docker.properties")
@ConfigurationProperties(prefix = "docker")
public class DockerProperties {
    private final String registryHostname;
    private final String registrySecretName;

    public DockerProperties(String registryHostname, String registrySecretName) {
        this.registryHostname = registryHostname;
        this.registrySecretName = registrySecretName;
    }

    public String getRegistryHostname() {
        return registryHostname;
    }

    public String getRegistrySecretName() {
        return registrySecretName;
    }
}
