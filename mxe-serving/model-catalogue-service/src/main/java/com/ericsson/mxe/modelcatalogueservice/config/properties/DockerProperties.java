package com.ericsson.mxe.modelcatalogueservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:docker.properties")
@ConfigurationProperties(prefix = "docker")
public class DockerProperties {
    private final DockerRegistryProperties registry;
    private final String hostName;

    public DockerProperties(@DefaultValue DockerRegistryProperties registry, String hostName) {
        this.registry = registry;
        this.hostName = hostName;
    }

    public DockerRegistryProperties getRegistry() {
        return registry;
    }

    public String getHostName() {
        return hostName;
    }
}
