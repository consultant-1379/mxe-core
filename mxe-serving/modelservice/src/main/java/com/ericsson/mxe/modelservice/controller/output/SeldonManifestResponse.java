package com.ericsson.mxe.modelservice.controller.output;

import com.ericsson.mxe.modelservice.deployer.response.DeployerServiceResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class SeldonManifestResponse {
    private String message;

    private DeployerServiceResponse data;

    public SeldonManifestResponse() {
        this.message = "";
    }

    public SeldonManifestResponse(String message) {
        this.message = message;
    }

    public SeldonManifestResponse(String message, DeployerServiceResponse data) {
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DeployerServiceResponse getData() {
        return data;
    }

    public void setData(DeployerServiceResponse data) {
        this.data = data;
    }

}
