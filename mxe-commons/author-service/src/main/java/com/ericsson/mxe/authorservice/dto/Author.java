package com.ericsson.mxe.authorservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;

public class Author {
    @NotEmpty
    private final String name;

    @NotEmpty
    private String publicKey;

    @JsonCreator
    public Author(@JsonProperty("name") final String name, @JsonProperty("publicKey") final String publicKey) {
        this.publicKey = publicKey;
        this.name = name;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getName() {
        return name;
    }
}
