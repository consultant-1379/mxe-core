package com.ericsson.mxe.jcat.command;

import com.ericsson.mxe.jcat.config.TestExecutionHost;
import com.ericsson.mxe.jcat.context.MxeJcatApplicationContextProvider;

public class Commands {

    private Commands() {}

    public static MxeFlowCommand mxeFlow(TestExecutionHost host) {
        return MxeJcatApplicationContextProvider.getApplicationContext(host).getBean(MxeFlowCommand.class);
    }

    public static MxeModelCommand mxeModel(TestExecutionHost host) {
        return MxeJcatApplicationContextProvider.getApplicationContext(host).getBean(MxeModelCommand.class);
    }

    public static MxeServiceCommand mxeService(TestExecutionHost host) {
        return MxeJcatApplicationContextProvider.getApplicationContext(host).getBean(MxeServiceCommand.class);
    }

    public static MxeTrainingCommand mxeTraining(TestExecutionHost host) {
        return MxeJcatApplicationContextProvider.getApplicationContext(host).getBean(MxeTrainingCommand.class);
    }

    public static KubectlCommand kubectl(TestExecutionHost host) {
        return MxeJcatApplicationContextProvider.getApplicationContext(host).getBean(KubectlCommand.class);
    }

    public static InxiCommand inxi(TestExecutionHost host) {
        return MxeJcatApplicationContextProvider.getApplicationContext(host).getBean(InxiCommand.class);
    }
}
