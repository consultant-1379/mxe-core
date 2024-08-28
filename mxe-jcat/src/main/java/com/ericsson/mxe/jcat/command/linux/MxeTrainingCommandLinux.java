package com.ericsson.mxe.jcat.command.linux;

import org.springframework.stereotype.Component;
import com.ericsson.mxe.jcat.command.MxeTrainingCommand;

@Component
public class MxeTrainingCommandLinux extends MxeTrainingCommand {

    public static final String COMMAND = "mxe-training";

    public MxeTrainingCommandLinux() {
        this(COMMAND);
    }

    public MxeTrainingCommandLinux(String command) {
        super(command);
    }

}
