package com.ericsson.mxe.keycloak.command;

import com.ericsson.mxe.keycloak.service.GatekeeperConfigService;
import com.ericsson.mxe.keycloak.service.KeycloakInitService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import java.util.Set;
import java.util.concurrent.Callable;

@Component
@Command(name = "init",
        description = "First checks if realm already exists and if not creates all resources and updates gatekeeper secret.")
public class Init implements Callable<Integer> {
    private final KeycloakInitService keycloakInitService;
    private final GatekeeperConfigService gatekeeperConfigService;

    public Init(KeycloakInitService keycloakInitService, GatekeeperConfigService gatekeeperConfigService) {
        this.keycloakInitService = keycloakInitService;
        this.gatekeeperConfigService = gatekeeperConfigService;
    }

    @Override
    public Integer call() {
        if (keycloakInitService.isInitNeeded()) {
            keycloakInitService.createResources(Set.of(KeycloakInitService.KeycloakResource.values()));
        }
        gatekeeperConfigService.update(keycloakInitService.getClientSecret());
        return 0;
    }
}
