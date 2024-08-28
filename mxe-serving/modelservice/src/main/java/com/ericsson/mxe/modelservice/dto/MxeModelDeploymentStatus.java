package com.ericsson.mxe.modelservice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public enum MxeModelDeploymentStatus {
    @JsonProperty("running")
    RUNNING,

    @JsonProperty("creating")
    CREATING,

    @JsonProperty("error")
    ERROR,

    @JsonProperty("unknown")
    UNKNOWN
}
