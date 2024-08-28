package com.ericsson.mxe.modelservice.modelcatalog;

public class DockerImage {
    private final String tag;
    private final String pullSecretName;

    public DockerImage(String tag, String pullSecretName) {
        this.tag = tag;
        this.pullSecretName = pullSecretName;
    }

    public String getTag() {
        return tag;
    }

    public String getPullSecretName() {
        return pullSecretName;
    }
}
