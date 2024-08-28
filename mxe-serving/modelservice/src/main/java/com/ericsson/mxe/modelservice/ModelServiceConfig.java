package com.ericsson.mxe.modelservice;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "model-service")
public class ModelServiceConfig {
    private String dockerRegistry;

    public void setDockerRegistry(String dockerRegistry) {
        this.dockerRegistry = dockerRegistry;
    }

    public String getDockerRegistry() {
        return dockerRegistry;
    }
}
