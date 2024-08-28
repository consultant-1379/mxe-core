package com.ericsson.mxe.modelcatalogueservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import jakarta.validation.constraints.NotNull;

@PropertySource("classpath:model-packager.properties")
@ConfigurationProperties(prefix = "model-packager")
public class ModelPackagerProperties {
    private final String image;
    private final String pullPolicy;
    private final String pullSecret;

    public ModelPackagerProperties(@NotNull String image, @NotNull String pullPolicy, @NotNull String pullSecret) {
        this.image = image;
        this.pullPolicy = pullPolicy;
        this.pullSecret = pullSecret;
    }

    public String getImage() {
        return this.image;
    }

    public String getPullPolicy() {
        return pullPolicy;
    }

    public String getPullSecret() {
        return pullSecret;
    }
}
