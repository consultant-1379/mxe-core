package com.ericsson.mxe.jcat.command.windows;

import com.ericsson.mxe.jcat.command.KubectlCommand;

public class KubectlCommandWindows extends KubectlCommand {

    public static final String COMMAND = "kubectl";

    public KubectlCommandWindows() {
        this(COMMAND);
    }

    public KubectlCommandWindows(String command) {
        super(command);
    }

}
