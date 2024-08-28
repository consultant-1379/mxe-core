package com.ericsson.mxe.modelcatalogueservice.dto.request;

import com.ericsson.mxe.backendservicescommon.dto.status.PackageStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateModelRequest {

    private final PackageStatus status;
    private final String message;
    private final String errorLog;

    @JsonCreator
    public UpdateModelRequest(@JsonProperty("status") final PackageStatus status,
            @JsonProperty("message") final String message, @JsonProperty("errorLog") final String errorLog) {
        this.status = status;
        this.message = message;
        this.errorLog = errorLog;
    }

    public PackageStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorLog() {
        return errorLog;
    }

}
