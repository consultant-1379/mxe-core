package com.ericsson.mxe.modelservice.dto.response;

public class ModelDeploymentResponse {

    public String message;

    public ModelDeploymentResponse() {
        this.message = "";
    }

    public ModelDeploymentResponse(String message) {
        this.message = message;
    }
}
