package com.ericsson.mxe.backendservicescommon.dto.status;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PackageStatus implements Status {
    @JsonProperty("available")
    Available,

    @JsonProperty("packaging")
    Packaging,

    @JsonProperty("error")
    Error
}
