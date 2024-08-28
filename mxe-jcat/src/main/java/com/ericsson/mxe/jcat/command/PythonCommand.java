package com.ericsson.mxe.jcat.command;

import java.time.Duration;

public class PythonCommand extends CustomCommand {

    public PythonCommand(String python) {
        super(python);
    }

    public PythonCommand runScript(String scriptWithParameters) {
        setParameter(scriptWithParameters);
        return this;
    }

    public PythonCommand withTimeout(Duration timeout) {
        setTimeoutMillis((int) timeout.toMillis());
        return this;
    }

    public static PythonCommand pythonCommand(String python) {
        return new PythonCommand(python);
    }

}
