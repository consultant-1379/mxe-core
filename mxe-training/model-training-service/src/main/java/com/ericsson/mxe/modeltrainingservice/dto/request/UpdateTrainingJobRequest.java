package com.ericsson.mxe.modeltrainingservice.dto.request;

import com.ericsson.mxe.modeltrainingservice.dto.status.TrainingJobStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateTrainingJobRequest {
    private TrainingJobStatus status;
    private String message;
    private String errorLog;

    @JsonCreator
    public UpdateTrainingJobRequest(@JsonProperty("status") final TrainingJobStatus status,
            @JsonProperty("message") final String message, @JsonProperty("errorLog") final String errorLog) {
        this.status = status;
        this.message = message;
        this.errorLog = errorLog;
    }

    public TrainingJobStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorLog() {
        return errorLog;
    }
}
