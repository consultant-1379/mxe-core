package com.ericsson.mxe.authorservice.dto;

public class VerifyResponse {
    public boolean result;
    public String name;

    public VerifyResponse(boolean result, String name) {
        this.result = result;
        this.name = name;
    }
}
