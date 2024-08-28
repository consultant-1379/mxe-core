package com.ericsson.mxe.securitycommon.accesscontrol;

public enum Action {
    READ("read"), ONBOARD("onboard"), UPDATE("update"), DELETE("delete"), CREATE("create"), MODIFY("modify"), ALL(
            "all");

    private final String value;

    public String getValue() {
        return value;
    }

    Action(String value) {
        this.value = value;
    }
}
