package com.ericsson.mxe.modeltrainingservice.dto.status;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TrainingJobStatus {
    @JsonProperty("running")
    Running,

    @JsonProperty("completed")
    Completed,

    @JsonProperty("failed")
    Failed
}
