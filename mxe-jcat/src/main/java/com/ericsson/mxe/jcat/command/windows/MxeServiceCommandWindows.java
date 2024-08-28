package com.ericsson.mxe.jcat.command.windows;

import com.ericsson.mxe.jcat.command.MxeServiceCommand;

public class MxeServiceCommandWindows extends MxeServiceCommand {

    public static final String COMMAND = "mxe-service.exe";

    public MxeServiceCommandWindows() {
        this(COMMAND);
    }

    public MxeServiceCommandWindows(String command) {
        super(command);
    }

}
