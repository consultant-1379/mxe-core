package com.ericsson.mxe.modelservice.controller.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestModelData {
    public String modelType;
}
