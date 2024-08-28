package com.ericsson.mxe.jcat.command;

import org.apache.commons.lang3.StringUtils;

public abstract class MxeCommand extends CustomCommand {

    static final String PARAMETER_TEMPLATE_LIST = " list";
    static final String PARAMETER_TEMPLATE_HELP = " --help";
    static final String PARAMETER_TEMPLATE_VERBOSE = " --verbose";
    static final String PARAMETER_TEMPLATE_CLUSTER = " --cluster %s";

    private String cluster;
    private String help;
    private String verbose;

    public MxeCommand(String command) {
        super(command);
    }

    public MxeCommand cluster(String name) {
        cluster = String.format(PARAMETER_TEMPLATE_CLUSTER, name);
        return this;
    }

    public MxeCommand help() {
        help = PARAMETER_TEMPLATE_HELP;
        return this;
    }

    public MxeCommand verbose() {
        verbose = PARAMETER_TEMPLATE_VERBOSE;
        return this;
    }

    @Override
    public String getSyntax() {
        String fullSyntax = super.getSyntax();
        fullSyntax += StringUtils.defaultIfEmpty(cluster, StringUtils.EMPTY);
        fullSyntax += StringUtils.defaultIfEmpty(help, StringUtils.EMPTY);
        fullSyntax += StringUtils.defaultIfEmpty(verbose, StringUtils.EMPTY);

        return fullSyntax;
    }
}
