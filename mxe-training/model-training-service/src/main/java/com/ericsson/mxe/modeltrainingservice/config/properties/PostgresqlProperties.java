package com.ericsson.mxe.modeltrainingservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import jakarta.validation.constraints.NotEmpty;

@PropertySource("classpath:postgresql.properties")
@ConfigurationProperties(prefix = "postgresql")
public class PostgresqlProperties {
    private final String service;
    private final String port;
    private final String db;
    private final String user;
    private final String password;

    public PostgresqlProperties(@NotEmpty String service, @NotEmpty String port, @NotEmpty String db,
            @NotEmpty String user, @NotEmpty String password) {
        this.service = service;
        this.port = port;
        this.db = db;
        this.user = user;
        this.password = password;
    }

    public String getService() {
        return service;
    }

    public String getPort() {
        return port;
    }

    public String getDb() {
        return db;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
