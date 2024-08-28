package com.ericsson.mxe.jcat.command.result;

public class CommandResult {

    private final int exitCode;
    private final String commandOutput;

    public CommandResult(final String commandOutput, final int exitCode) {
        this.commandOutput = commandOutput;
        this.exitCode = exitCode;
    }

    public String getCommandOutput() {
        return commandOutput;
    }

    public int getExitCode() {
        return exitCode;
    }
}
