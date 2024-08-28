package com.ericsson.mxe.modelservice.deployer.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeleteDeploymentResponse {
    public Boolean status;
}
