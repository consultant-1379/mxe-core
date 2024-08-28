package com.ericsson.mxe.modelcatalogueservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import jakarta.validation.constraints.NotEmpty;

@PropertySource("classpath:model-catalogue-service.properties")
@ConfigurationProperties(prefix = "model-catalogue-service")
public class ModelCatalogueServiceProperties {
    private final String name;
    private final String instanceName;
    private final String version;
    private final String dockerfile;

    public ModelCatalogueServiceProperties(@NotEmpty String name, @NotEmpty String instanceName,
            @NotEmpty String version, String dockerfile) {
        this.name = name;
        this.instanceName = instanceName;
        this.version = version;
        this.dockerfile = dockerfile;
    }

    public String getName() {
        return name;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getVersion() {
        return version;
    }

    public String getDockerfile() {
        return dockerfile;
    }
}
