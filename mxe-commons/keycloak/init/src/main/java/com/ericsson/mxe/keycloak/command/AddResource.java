package com.ericsson.mxe.keycloak.command;

import com.ericsson.mxe.keycloak.service.KeycloakInitService;
import com.ericsson.mxe.keycloak.service.KeycloakInitService.KeycloakResource;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.util.Set;
import java.util.concurrent.Callable;

@Component
@Command(name = "add-resource", description = "Creates the specified resources in the realm.")
public class AddResource implements Callable<Integer> {
    private final KeycloakInitService keycloakInitService;

    private Set<KeycloakResource> resourceNames;

    public AddResource(KeycloakInitService keycloakInitService) {
        this.keycloakInitService = keycloakInitService;
    }

    @Option(names = {"-r", "--resource-names"}, required = true, split = ",",
            description = "Valid values: ${COMPLETION-CANDIDATES}")
    public void setResourceNames(Set<KeycloakResource> resourceNames) {
        this.resourceNames = resourceNames;
    }

    @Override
    public Integer call() {
        keycloakInitService.createResources(resourceNames);
        return 0;
    }

}
