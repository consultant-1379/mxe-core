package com.ericsson.mxe.modelcatalogueservice.config.properties;



public class DockerRegistryProperties {
    private final String hostname;
    private final String externalHostname;
    private final String secretName;
    private final String username;
    private final String password;

    public DockerRegistryProperties(String hostname, String externalHostname, String secretName, String username,
            String password) {
        this.hostname = hostname;
        this.externalHostname = externalHostname;
        this.secretName = secretName;
        this.username = username;
        this.password = password;
    }

    public String getExternalHostname() {
        return externalHostname;
    }

    public String getSecretName() {
        return secretName;
    }

    public String getHostname() {
        return hostname;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
