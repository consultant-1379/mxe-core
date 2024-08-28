package com.ericsson.mxe.modeltrainingservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import jakarta.validation.constraints.NotNull;

@PropertySource("classpath:model-trainer.properties")
@ConfigurationProperties(prefix = "model-trainer")
public class ModelTrainerProperties {
    private final String pullPolicy;

    public ModelTrainerProperties(@NotNull String pullPolicy) {
        this.pullPolicy = pullPolicy;
    }

    public String getPullPolicy() {
        return pullPolicy;
    }
}
