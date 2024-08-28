package com.ericsson.mxe.modelservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:seldon.properties")
@ConfigurationProperties(prefix = "seldon")
public class SeldonProperties {
    private final SeldonCrdProperties crd;
    private final String engineServiceAccountName;

    public SeldonProperties(SeldonCrdProperties crd, String engineServiceAccountName) {
        this.crd = crd;
        this.engineServiceAccountName = engineServiceAccountName;
    }

    public SeldonCrdProperties getCrd() {
        return crd;
    }

    public String getEngineServiceAccountName() {
        return engineServiceAccountName;
    }
}
