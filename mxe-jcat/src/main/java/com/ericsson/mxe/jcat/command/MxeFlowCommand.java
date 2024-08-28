package com.ericsson.mxe.jcat.command;

public abstract class MxeFlowCommand extends MxeCommand {

    private static final String PARAMETER_TEMPLATE_DELETE_DEPLOYMENT = " delete deployment --name %s";
    private static final String PARAMETER_TEMPLATE_DELETE_FLOW = " delete flow --name %s";
    private static final String PARAMETER_TEMPLATE_DEPLOY = " deploy --name %s --flow-name %s%s";
    private static final String PARAMETER_TEMPLATE_INSTANCES = " --instances %d";
    private static final String PARAMETER_TEMPLATE_SCALE = " scale --name %s" + PARAMETER_TEMPLATE_INSTANCES;
    private static final String PARAMETER_TEMPLATE_LIST_DEPLOYMENTS = " list deployments";
    private static final String PARAMETER_TEMPLATE_LIST_FLOWS = " list flows";
    private static final String PARAMETER_TEMPLATE_ONBOARD = " onboard --file %s --name %s";
    private static final String PARAMETER_TEMPLATE_VERSION = " version";

    public MxeFlowCommand(String command) {
        super(command);
    }

    public MxeFlowCommand deploy(final String name, final String flowName) {
        return deploy(name, flowName, null);
    }

    public MxeFlowCommand deploy(final String name, final String flowName, final Integer instances) {
        if (instances != null) {
            setParameter(String.format(PARAMETER_TEMPLATE_DEPLOY, name, flowName,
                    String.format(PARAMETER_TEMPLATE_INSTANCES, instances)));
        } else {
            setParameter(String.format(PARAMETER_TEMPLATE_DEPLOY, name, flowName, ""));
        }
        return this;
    }

    public MxeFlowCommand scaleDeployment(final String name, final Integer instances) {
        setParameter(String.format(PARAMETER_TEMPLATE_SCALE, name, instances));
        return this;
    }

    public MxeFlowCommand deleteDeployment(final String name) {
        setParameter(String.format(PARAMETER_TEMPLATE_DELETE_DEPLOYMENT, name));
        return this;
    }

    public MxeFlowCommand deleteFlow(final String name) {
        setParameter(String.format(PARAMETER_TEMPLATE_DELETE_FLOW, name));
        return this;
    }

    public MxeFlowCommand listDeployments() {
        setParameter(PARAMETER_TEMPLATE_LIST_DEPLOYMENTS);
        return this;
    }

    public MxeFlowCommand listFlows() {
        setParameter(PARAMETER_TEMPLATE_LIST_FLOWS);
        return this;
    }

    public MxeFlowCommand onboard(final String file, final String name) {
        setParameter(String.format(PARAMETER_TEMPLATE_ONBOARD, file, name));
        return this;
    }

    public MxeFlowCommand version() {
        setParameter(PARAMETER_TEMPLATE_VERSION);
        return this;
    }

    public MxeFlowCommand list() {
        setParameter(PARAMETER_TEMPLATE_LIST);
        return this;
    }
}
