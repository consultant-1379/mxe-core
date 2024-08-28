package com.ericsson.mxe.modelservice.config.properties;

public class GitRepoProperties {

    private final String url;
    private final String path;
    private final String branch;

    public GitRepoProperties(String url, String path, String branch) {
        this.url = url;
        this.path = path;
        this.branch = branch;
    }

    public String getUrl() {
        return url;
    }

    public String getPath() {
        return path;
    }

    public String getBranch() {
        return branch;
    }

}
