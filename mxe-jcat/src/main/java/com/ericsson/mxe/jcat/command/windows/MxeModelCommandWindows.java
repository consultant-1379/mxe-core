package com.ericsson.mxe.jcat.command.windows;

import com.ericsson.mxe.jcat.command.MxeModelCommand;

public class MxeModelCommandWindows extends MxeModelCommand {

    public static final String COMMAND = "mxe-model.exe";

    public MxeModelCommandWindows() {
        this(COMMAND);
    }

    public MxeModelCommandWindows(String command) {
        super(command);
    }

}
