package com.ericsson.mxe.authorservice.config;

import com.ericsson.mxe.authorservice.config.properties.PostgresqlProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import javax.sql.DataSource;

@Configuration
@Profile("production")
public class DbConfig {
    private final PostgresqlProperties postgresqlProperties;

    public DbConfig(PostgresqlProperties postgresqlProperties) {
        this.postgresqlProperties = postgresqlProperties;
    }

    @Bean
    public DataSource dataSource() {
        final String url = "jdbc:postgresql://" + postgresqlProperties.getService() + ":5432/"
                + postgresqlProperties.getDb() + System.getenv("JDBC_PARAMS");
        return DataSourceBuilder.create().url(url).username(postgresqlProperties.getUser())
                .password(postgresqlProperties.getPassword()).driverClassName("org.postgresql.Driver").build();
    }
}
