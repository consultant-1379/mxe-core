package com.ericsson.mxe.jcat.command.linux;

import com.ericsson.mxe.jcat.command.InxiCommand;

public class InxiCommandLinux extends InxiCommand {

    private static final String COMMAND = "inxi";

    public InxiCommandLinux() {
        super(COMMAND);
    }
}
