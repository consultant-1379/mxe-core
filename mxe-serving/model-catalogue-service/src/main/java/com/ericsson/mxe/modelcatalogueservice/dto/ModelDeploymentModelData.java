package com.ericsson.mxe.modelcatalogueservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelDeploymentModelData {
    private final String id;
    private final String version;
    private final String endpointType;

    @JsonCreator
    public ModelDeploymentModelData(@JsonProperty("id") final String id, @JsonProperty("version") final String version,
            @JsonProperty("endpointType") final String endpointType) {
        this.id = id;
        this.version = version;
        this.endpointType = endpointType;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getEndpointType() {
        return endpointType;
    }
}
