package com.ericsson.mxe.jcat.command.windows;

import com.ericsson.mxe.jcat.command.MxeFlowCommand;

public class MxeFlowCommandWindows extends MxeFlowCommand {

    public static final String COMMAND = "mxe-flow.exe";

    public MxeFlowCommandWindows() {
        this(COMMAND);
    }

    public MxeFlowCommandWindows(String command) {
        super(command);
    }

}
