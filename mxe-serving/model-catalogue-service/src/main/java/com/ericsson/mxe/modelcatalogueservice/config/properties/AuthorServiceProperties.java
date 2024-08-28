package com.ericsson.mxe.modelcatalogueservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import jakarta.validation.constraints.NotEmpty;

@PropertySource("classpath:author-service.properties")
@ConfigurationProperties(prefix = "author-service")
public class AuthorServiceProperties {
    private final String hostName;
    private final String portName;

    public AuthorServiceProperties(@NotEmpty String hostName, @NotEmpty String portName) {
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
