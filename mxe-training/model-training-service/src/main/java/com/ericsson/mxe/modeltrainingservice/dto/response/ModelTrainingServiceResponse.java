package com.ericsson.mxe.modeltrainingservice.dto.response;

public class ModelTrainingServiceResponse {
    public String message;
    public String additionalInfo;

    public ModelTrainingServiceResponse(final String message) {
        this.message = message;
        this.additionalInfo = null;
    }

    public ModelTrainingServiceResponse(final String message, final String additionalInfo) {
        this.message = message;
        this.additionalInfo = additionalInfo;
    }

    public ModelTrainingServiceResponse() {}
}
