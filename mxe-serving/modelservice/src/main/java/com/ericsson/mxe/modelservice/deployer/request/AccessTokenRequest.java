package com.ericsson.mxe.modelservice.deployer.request;

public class AccessTokenRequest {
    private String username;
    private String password;
    private boolean ssoMode = true;
    private String ssoHost;

    public AccessTokenRequest(String username, String password, boolean ssoMode, String ssoHost) {
        this.username = username;
        this.password = password;
        this.ssoMode = ssoMode;
        this.ssoHost = ssoHost;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSsoMode() {
        return ssoMode;
    }

    public void setSsoMode(boolean ssoMode) {
        this.ssoMode = ssoMode;
    }

    public String getSsoHost() {
        return ssoHost;
    }

    public void setSsoHost(String ssoHost) {
        this.ssoHost = ssoHost;
    }
}
