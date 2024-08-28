package com.ericsson.mxe.modelcatalogueservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "pypiservice")
public class PypiServiceConfig {
    private String internalServer;
    private String externalServer;

    public String getInternalServer() {
        return internalServer;
    }

    public String getExternalServer() {
        return externalServer;
    }

    public void setInternalServer(String internalServer) {
        this.internalServer = internalServer;
    }

    public void setExternalServer(String externalServer) {
        this.externalServer = externalServer;
    }
}
