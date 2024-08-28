package com.ericsson.mxe.jcat.test;

import com.ericsson.mxe.jcat.command.Commands;
import com.ericsson.mxe.jcat.command.MxeFlowCommand;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import org.apache.commons.lang3.StringUtils;
import se.ericsson.jcat.fw.logging.JcatLoggingApi;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.*;
import static se.ericsson.jcat.fluentassert.JcatFluentAssertApi.saveAssertThat;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.saveAssertTrue;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.*;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestWarning;

public class MxeFlowTestHelper {

    private MxeFlowTestHelper() {}

    static void checkIfFlowNotExists(MxeCliDriver mxeCliDriver, String flowName) {
        setTestStepBegin("Check if flow '%s' NOT exists", flowName);

        MxeFlowCommand listFlowsCmd = Commands.mxeFlow(mxeCliDriver.getTestExecutionHost()).listFlows();
        Optional<CommandResult> resultOpt = executeCommand(mxeCliDriver, listFlowsCmd);

        resultOpt.map(r -> String.valueOf(r.getCommandOutput()))
                .ifPresent(r -> saveAssertThat(r.split("\n")).doesNotContain(flowName));
        setTestStepEnd();
    }

    static void checkIfFlowExists(MxeCliDriver mxeCliDriver, String flowName) {
        setTestStepBegin("Check if flow '%s' exists", flowName);

        MxeFlowCommand listFlowsCmd = Commands.mxeFlow(mxeCliDriver.getTestExecutionHost()).listFlows();
        Optional<CommandResult> resultOpt = executeCommand(mxeCliDriver, listFlowsCmd);
        resultOpt.map(r -> String.valueOf(r.getCommandOutput()))
                .ifPresent(r -> saveAssertThat(r.split("\n")).contains(flowName));
        setTestStepEnd();
    }

    static void checkIfFlowDeploymentNotExists(MxeCliDriver mxeCliDriver, String flowDeploymentName) {
        setTestStepBegin("Check if flow deployment '%s' NOT exists", flowDeploymentName);
        Map<String, String> flowDeployment =
                getFlowDeploymentFromList(getFlowDeploymentList(mxeCliDriver), HEADER_ELEMENT_NAME, flowDeploymentName);

        saveAssertTrue("Flow deployment with name " + flowDeploymentName + " found", flowDeployment == null);

        setTestStepEnd();
    }

    static void checkIfFlowDeploymentExists(MxeCliDriver mxeCliDriver, String flowDeploymentName) {
        checkIfFlowDeploymentExists(mxeCliDriver, flowDeploymentName, null);
    }

    static void checkIfFlowDeploymentExists(MxeCliDriver mxeCliDriver, String flowDeploymentName, Integer instances) {
        setTestStepBegin("Check if flow deployment '%s' exists", flowDeploymentName);
        Map<String, String> flowDeployment =
                getFlowDeploymentFromList(getFlowDeploymentList(mxeCliDriver), HEADER_ELEMENT_NAME, flowDeploymentName);

        saveAssertTrue(
                "Flow deployment with name " + flowDeploymentName
                        + (instances == null ? "" : " and instances " + instances) + " does not exist",
                flowDeployment != null && (instances == null
                        || Integer.parseInt(flowDeployment.get(HEADER_ELEMENT_INSTANCES)) == instances));

        setTestStepEnd();
    }

    static List<Map<String, String>> getFlowDeploymentList(final MxeCliDriver mxeCliDriver) {
        return getList(mxeCliDriver, Commands.mxeFlow(mxeCliDriver.getTestExecutionHost()).listDeployments());
    }

    static Map<String, String> getFlowDeploymentFromList(List<Map<String, String>> flowOrDeploymentList,
            String filterKey, String filterValue) {
        for (Map<String, String> model : flowOrDeploymentList) {
            if (model.get(filterKey) != null && model.get(filterKey).equals(filterValue)) {
                return model;
            }
        }
        return null;
    }

    static void listFlowsInStep(MxeCliDriver mxeCliDriver) {
        setTestStepBegin("List flows");

        executeCommand(mxeCliDriver, Commands.mxeFlow(mxeCliDriver.getTestExecutionHost()).listFlows())
                .map(r -> String.valueOf(r.getCommandOutput()))
                .ifPresent(s -> JcatLoggingApi.setTestInfo("Flows:\n" + s));
        setTestStepEnd();
    }

    static void listFlowDeploymentsInStep(MxeCliDriver mxeCliDriver) {
        setTestStepBegin("List flow deployments");

        executeCommand(mxeCliDriver, Commands.mxeFlow(mxeCliDriver.getTestExecutionHost()).listDeployments())
                .map(r -> String.valueOf(r.getCommandOutput()))
                .ifPresent(s -> JcatLoggingApi.setTestInfo("Flow deployments:\n" + s));
        setTestStepEnd();
    }


}
