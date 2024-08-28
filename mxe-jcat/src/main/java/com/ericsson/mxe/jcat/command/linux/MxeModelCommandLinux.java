package com.ericsson.mxe.jcat.command.linux;

import org.springframework.stereotype.Component;
import com.ericsson.mxe.jcat.command.MxeModelCommand;

@Component
public class MxeModelCommandLinux extends MxeModelCommand {

    public static final String COMMAND = "mxe-model";

    public MxeModelCommandLinux() {
        this(COMMAND);
    }

    public MxeModelCommandLinux(String command) {
        super(command);
    }

}
