package com.ericsson.mxe.keycloak;

import com.ericsson.mxe.keycloak.command.Realm;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = {"com.ericsson.mxe.keycloak.config.properties"})
@ComponentScan(basePackages = {"com.ericsson.mxe"})
public class KeycloakInitApplication implements CommandLineRunner, ExitCodeGenerator {

    private final IFactory factory;
    private final Realm commands;
    private int exitCode;

    public KeycloakInitApplication(IFactory factory, Realm commands) {
        this.factory = factory;
        this.commands = commands;
    }

    @Override
    public void run(String... args) {
        final CommandLine cmd = new CommandLine(commands, factory);
        cmd.setCaseInsensitiveEnumValuesAllowed(true);
        exitCode = cmd.execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public static void main(String[] args) {
        final SpringApplication app = new SpringApplication(KeycloakInitApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setBannerMode(Banner.Mode.OFF);
        System.exit(SpringApplication.exit(app.run(args)));
    }
}

