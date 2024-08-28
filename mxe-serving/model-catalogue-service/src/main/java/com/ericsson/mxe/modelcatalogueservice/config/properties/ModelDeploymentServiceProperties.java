package com.ericsson.mxe.modelcatalogueservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import jakarta.validation.constraints.NotNull;

@PropertySource("classpath:model-deployment-service.properties")
@ConfigurationProperties(prefix = "model-deployment-service")
public class ModelDeploymentServiceProperties {
    private final String hostName;
    private final String portName;

    public ModelDeploymentServiceProperties(@NotNull String hostName, @NotNull String portName) {
        this.hostName = hostName;
        this.portName = portName;
    }

    public String getHostName() {
        return hostName;
    }

    public String getPortName() {
        return portName;
    }
}
