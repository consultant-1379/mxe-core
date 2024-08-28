package com.ericsson.mxe.modeltrainingservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import jakarta.validation.constraints.NotEmpty;

@PropertySource("classpath:model-training-service.properties")
@ConfigurationProperties(prefix = "model-training-service")
public class ModelTrainingServiceProperties {
    private final String name;
    private final String instanceName;
    private final String version;
    private final String dockerfile;

    public ModelTrainingServiceProperties(@NotEmpty String name, @NotEmpty String instanceName,
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
