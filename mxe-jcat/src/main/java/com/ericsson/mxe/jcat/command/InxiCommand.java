package com.ericsson.mxe.jcat.command;

public abstract class InxiCommand extends CustomCommand {

    public InxiCommand(String command) {
        super(command);
    }

    public CustomCommand cpu() {
        // only collect cpu data, add extra output, output as json, print to stderr (required when json output set),
        // drop unnecessary printouts, remove coloring data
        setParameter("  --cpu -x --output json --output-file /dev/stderr 2>&1 >/dev/null |sed 's/[0-9][0-9][0-9]#//g'");
        return this;
    }
}
