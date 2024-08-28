package com.ericsson.mxe.jcat.command.linux;

import com.ericsson.mxe.jcat.command.MxeFlowCommand;

public class MxeFlowCommandLinux extends MxeFlowCommand {

    public static final String COMMAND = "mxe-flow";

    public MxeFlowCommandLinux() {
        this(COMMAND);
    }

    public MxeFlowCommandLinux(String command) {
        super(command);
    }

}
