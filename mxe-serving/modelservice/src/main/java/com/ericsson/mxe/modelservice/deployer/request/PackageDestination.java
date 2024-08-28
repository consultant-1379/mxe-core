package com.ericsson.mxe.modelservice.deployer.request;

public class PackageDestination {
    private String namespace;
    private String server;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
