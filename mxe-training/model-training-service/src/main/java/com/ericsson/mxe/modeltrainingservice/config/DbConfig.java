package com.ericsson.mxe.modeltrainingservice.config;

import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.ericsson.mxe.backendservicescommon.config.properties.KubernetesServiceProperties;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.backendservicescommon.kubernetes.ServicePortResolverService;
import com.ericsson.mxe.modeltrainingservice.config.properties.PostgresqlProperties;

@Configuration
@Profile("production")
@ComponentScan(basePackages = {"com.ericsson.mxe.backendservicescommon"})
public class DbConfig {
    private final KubernetesServiceProperties kubernetesServiceProperties;
    private final ServicePortResolverService servicePortResolverService;
    private final PostgresqlProperties postgresqlProperties;

    public DbConfig(final KubernetesServiceProperties kubernetesServiceProperties,
            final ServicePortResolverService servicePortResolverService, PostgresqlProperties postgresqlProperties) {
        this.kubernetesServiceProperties = kubernetesServiceProperties;
        this.servicePortResolverService = servicePortResolverService;
        this.postgresqlProperties = postgresqlProperties;
    }

    @Bean
    public DataSource dataSource() {
        String url;

        if (kubernetesServiceProperties.isLocalTestEnabled()) {
            url = "jdbc:postgresql://" + postgresqlProperties.getService() + ":" + postgresqlProperties.getPort() + "/"
                    + postgresqlProperties.getDb() + System.getenv("JDBC_PARAMS");
        } else {
            final Optional<Integer> port = this.servicePortResolverService.resolve(postgresqlProperties.getService(),
                    postgresqlProperties.getPort(), "tcp");

            if (port.isPresent()) {
                url = "jdbc:postgresql://" + postgresqlProperties.getService() + ":" + port.get().toString() + "/"
                        + postgresqlProperties.getDb() + System.getenv("JDBC_PARAMS");
            } else {
                throw new MxeInternalException("Database service (" + postgresqlProperties.getService() + ") with port "
                        + postgresqlProperties.getPort() + " is not found");
            }
        }

        return DataSourceBuilder.create().url(url).username(postgresqlProperties.getUser())
                .password(postgresqlProperties.getPassword()).driverClassName("org.postgresql.Driver").build();
    }
}
