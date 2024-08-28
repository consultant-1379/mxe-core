package com.ericsson.mxe.jcat.command.linux;

import com.ericsson.mxe.jcat.command.KubectlCommand;

public class KubectlCommandLinux extends KubectlCommand {

    public static final String COMMAND = "kubectl";

    public KubectlCommandLinux() {
        this(COMMAND);
    }

    public KubectlCommandLinux(String command) {
        super(command);
    }

}
