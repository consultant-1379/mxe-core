package com.ericsson.mxe.modelservice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public enum MxeModelDeploymentType {
    @JsonProperty("model")
    MODEL,

    @JsonProperty("static")
    STATIC,

    @JsonProperty("dynamic")
    DYNAMIC,

    @JsonProperty("unknown")
    UNKNOWN
}
