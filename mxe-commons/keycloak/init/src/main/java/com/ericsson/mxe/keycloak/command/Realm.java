package com.ericsson.mxe.keycloak.command;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Component
@Command(name = "realm", synopsisSubcommandLabel = "(init | add-resource)",
        subcommands = {Init.class, AddResource.class})
public class Realm implements Runnable {
    @Spec
    CommandSpec spec;

    @Override
    public void run() {
        throw new ParameterException(spec.commandLine(), "Missing required subcommand.");
    }
}
