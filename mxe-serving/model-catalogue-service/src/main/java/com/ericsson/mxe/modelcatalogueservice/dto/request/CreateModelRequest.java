package com.ericsson.mxe.modelcatalogueservice.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CreateModelRequest extends CreateRequest {

    private Boolean stateful;

    public CreateModelRequest(final String id, final String version, final String title, final String author,
            final String description, final String image, final String icon, final String dockerRegistrySecretName) {
        this(id, version, title, author, description, image, icon, dockerRegistrySecretName, null, null, false);
    }

    @JsonCreator
    public CreateModelRequest(final String id, final String version, final String title, final String author,
            final String description, final String image, final String icon, final String dockerRegistrySecretName,
            final String signedByPublicKey, final String signedByName, final Boolean stateful) {
        super(id, version, title, author, description, image, icon, dockerRegistrySecretName, signedByPublicKey,
                signedByName);
        this.stateful = stateful == null ? false : stateful;
    }

    public Boolean isStateful() {
        return stateful;
    }
}
