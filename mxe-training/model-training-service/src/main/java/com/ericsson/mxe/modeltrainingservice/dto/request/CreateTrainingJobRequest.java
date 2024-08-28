package com.ericsson.mxe.modeltrainingservice.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class CreateTrainingJobRequest {
    @NotNull
    private final String packageId;
    @NotNull
    private final String packageVersion;

    @JsonCreator
    public CreateTrainingJobRequest(@JsonProperty("packageId") final String packageId,
            @JsonProperty("packageVersion") final String packageVersion) {
        this.packageId = packageId;
        this.packageVersion = packageVersion;
    }

    public String getPackageId() {
        return packageId;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    @Override
    public String toString() {
        return "{" + "packageId='" + packageId + '\'' + ", packageVersion='" + packageVersion + '\'' + '}';
    }
}
