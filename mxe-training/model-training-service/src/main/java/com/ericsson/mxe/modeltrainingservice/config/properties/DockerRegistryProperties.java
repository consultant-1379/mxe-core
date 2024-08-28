package com.ericsson.mxe.modeltrainingservice.config.properties;

public class DockerRegistryProperties {
    private final String hostname;
    private final String externalHostname;
    private final String externalSecretName;
    private final String secretName;
    private final String username;
    private final String password;

    public DockerRegistryProperties(String hostname, String externalHostname, String externalSecretName,
            String secretName, String username, String password) {
        this.hostname = hostname;
        this.externalHostname = externalHostname;
        this.externalSecretName = externalSecretName;
        this.secretName = secretName;
        this.username = username;
        this.password = password;
    }

    public String getExternalHostname() {
        return externalHostname;
    }

    public String getExternalSecretName() {
        return externalSecretName;
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
