package com.ericsson.mxe.securitycommon.accesscontrol;

public enum TargetType {
    MODELS("models"), MODEL_SERVICES("model-services"), ALL("all");

    private String value;

    public String getValue() {
        return value;
    }

    TargetType(String value) {
        this.value = value;
    }
}
