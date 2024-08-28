package com.ericsson.mxe.jcat.command;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.ericsson.commonlibrary.command.api.executor.Command;

public abstract class CustomCommand implements Command {

    private String command;
    private String parameter;
    private int timeoutMillis = -1;

    public CustomCommand(final String command) {
        this(command, StringUtils.EMPTY);
    }

    public CustomCommand(final String command, final String parameter) {
        this.command = command;
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(final String parameter) {
        this.parameter = parameter;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public String getSyntax() {
        final String param = getParameter();
        if (StringUtils.isEmpty(param)) {
            return command;
        } else {
            return command + " " + param;
        }
    }

    public List<String> getSyntaxAsList() {
        return Arrays.asList(getSyntax().split("\\s+"));
    }
}
