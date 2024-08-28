package com.ericsson.mxe.jcat.command;

import java.time.Duration;

public class PipCommand extends CustomCommand {

    public PipCommand(String pip) {
        super(pip);
    }

    public PipCommand installRequirement(String reqFile) {
        setParameter(" install --user --requirement " + reqFile);
        return this;
    }

    public PipCommand withTimeout(Duration timeout) {
        setTimeoutMillis((int) timeout.toMillis());
        return this;
    }

    public static PipCommand pipCommand(String pip) {
        return new PipCommand(pip);
    }
}
