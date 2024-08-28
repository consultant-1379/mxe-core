package com.ericsson.mxe.modeltrainingservice.dto.request;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class CreateRequest {

    @NotNull
    private final String id;
    @NotNull
    private final String version;
    private final String title;
    private final String author;
    private final String description;
    @NotNull
    private final String image;
    private final String icon;
    private final String dockerRegistrySecretName;
    private final String signedByPublicKey;
    private final String signedByName;

    @JsonCreator
    protected CreateRequest(@JsonProperty("id") final String id, @JsonProperty("version") final String version,
            @JsonProperty("title") final String title, @JsonProperty("author") final String author,
            @JsonProperty("description") final String description, @JsonProperty("image") final String image,
            @JsonProperty("icon") final String icon,
            @JsonProperty("dockerRegistrySecretName") final String dockerRegistrySecretName,
            @JsonProperty("signedByPublicKey") final String signedByPublicKey,
            @JsonProperty("signedByName") final String signedByName) {
        this.id = id;
        this.version = version;
        this.title = title;
        this.author = author;
        this.description = description;
        this.image = image;
        this.icon = icon;
        this.dockerRegistrySecretName = dockerRegistrySecretName;
        this.signedByName = signedByName;
        this.signedByPublicKey = signedByPublicKey;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public String getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDockerRegistrySecretName() {
        return dockerRegistrySecretName;
    }

    public String getSignedByPublicKey() {
        return signedByPublicKey;
    }

    public String getSignedByName() {
        return signedByName;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}
