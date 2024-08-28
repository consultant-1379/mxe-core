package com.ericsson.mxe.backendservicescommon.dto;

public class SingleMessageResponse {
    private final String message;

    public SingleMessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
