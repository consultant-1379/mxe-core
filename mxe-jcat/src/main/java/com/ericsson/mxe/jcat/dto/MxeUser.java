package com.ericsson.mxe.jcat.dto;

public class MxeUser {

    private String userName;
    private String password;

    public MxeUser(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
