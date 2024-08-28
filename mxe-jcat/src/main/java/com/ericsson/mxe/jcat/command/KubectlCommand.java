package com.ericsson.mxe.jcat.command;

public abstract class KubectlCommand extends CustomCommand {

    private static final String PARAMETER_TEMPLATE_GET_PODS = " get pods --namespace=%s";
    private static final String PARAMETER_TEMPLATE_PORT_FORWARD =
            " port-forward --address 0.0.0.0 %s %d:%d --namespace=%s";

    public KubectlCommand(String command) {
        super(command);
    }

    public CustomCommand getPods(final String nameSpace) {
        setParameter(String.format(PARAMETER_TEMPLATE_GET_PODS, nameSpace));
        return this;
    }

    public CustomCommand portForward(final String pod, final int localPort, final int podPort, final String nameSpace) {
        setParameter(String.format(PARAMETER_TEMPLATE_PORT_FORWARD, pod, localPort, podPort, nameSpace));
        return this;
    }
}
