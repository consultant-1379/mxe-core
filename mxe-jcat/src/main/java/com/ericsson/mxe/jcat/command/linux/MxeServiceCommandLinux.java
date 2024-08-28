package com.ericsson.mxe.jcat.command.linux;

import org.springframework.stereotype.Component;
import com.ericsson.mxe.jcat.command.MxeServiceCommand;

@Component
public class MxeServiceCommandLinux extends MxeServiceCommand {

    public static final String COMMAND = "mxe-service";

    public MxeServiceCommandLinux() {
        this(COMMAND);
    }

    public MxeServiceCommandLinux(String command) {
        super(command);
    }

}
