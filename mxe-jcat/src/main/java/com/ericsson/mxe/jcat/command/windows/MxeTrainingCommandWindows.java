package com.ericsson.mxe.jcat.command.windows;

import com.ericsson.mxe.jcat.command.MxeTrainingCommand;

public class MxeTrainingCommandWindows extends MxeTrainingCommand {

    public static final String COMMAND = "mxe-training.exe";

    public MxeTrainingCommandWindows() {
        this(COMMAND);
    }

    public MxeTrainingCommandWindows(String command) {
        super(command);
    }

}
