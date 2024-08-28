package com.ericsson.mxe.jcat.test;

import com.ericsson.mxe.jcat.command.Commands;
import com.ericsson.mxe.jcat.command.CustomCommand;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.JcatClass;
import se.ericsson.jcat.fw.annotations.JcatMethod;
import java.util.regex.Pattern;
import static com.ericsson.mxe.jcat.test.MxeFlowTestHelper.*;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.*;

/**
 * @JcatDocChapterDescription Chapter covering flow function tests.
 */
@JcatClass(chapterName = "Flow Function Tests")
public class MxeFlowTest extends MxeTestBase {

    /**
     * @JcatTcDescription List MXE flow commands
     * @JcatTcPreconditions None
     * @JcatTcInstruction The testcase is about running the mxe-flow CLI to print list related help messages using
     *                    default cluster
     * @JcatTcAction mxe-flow list --help --verbose
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction mxe-flow list --verbose
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction mxe-flow list --help
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction mxe-flow list
     * @JcatTcActionResult Help message is printed
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-FLOW-COMMANDS", testTitle = "List MXE flow commands")
    public void testMxeFlowPrintHelp() {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            executeInStep("Flow list help verbose", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).list().help().verbose());

            executeInStep("Flow list verbose", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).list().verbose());

            executeInStep("Flow list help", mxeCliDriver, () -> Commands.mxeFlow(testExecutionHost).list().help());

            executeInStep("Flow list", mxeCliDriver, () -> Commands.mxeFlow(testExecutionHost).list());
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Onboard and deploy a flow on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction Test MXE flow onboard and deploy commands
     * @JcatTcAction Onboard the flow
     * @JcatTcActionResult Flow is onboarded successfully
     * @JcatTcAction List onboarded flows
     * @JcatTcActionResult Previously onboarded flow appears in the deployed flow list
     * @JcatTcAction Deploy the onboarded flow
     * @JcatTcActionResult Flow deployed successfully
     * @JcatTcAction Check if flow deployment exists
     * @JcatTcActionResult Previously deployed flow appears in the deployed flows list
     * @JcatTcAction Delete flow deployment
     * @JcatTcActionResult Flow deployment deleted successfully
     * @JcatTcAction Check if flow deployment exists
     * @JcatTcActionResult Flow must not appear in the deployed flows list
     * @JcatTcAction Delete the flow
     * @JcatTcActionResult Flow deleted successfully
     * @JcatTcAction List onboarded flows
     * @JcatTcActionResult Flow must not appear in the deployed flow list
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-FLOW-ONBOARD-AND-DEPLOY", testTitle = "Test MXE flow onboard and deploy commands")
    @Parameters({"flowName", "flowFile", "flowDeploymentName"})
    public void testMxeFlowOnboardAndDeploy(String flowName, String flowFile, String flowDeploymentName) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            executeInStep("Onboard flow '" + flowName + "'", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).onboard(flowFile, flowName));

            checkIfFlowExists(mxeCliDriver, flowName);

            executeInStep("Deploy flow as '" + flowDeploymentName + "'", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).deploy(flowDeploymentName, flowName));

            checkIfFlowDeploymentExists(mxeCliDriver, flowDeploymentName);

            executeInStep("Delete flow deployment '" + flowDeploymentName + "'", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).deleteDeployment(flowDeploymentName));

            sleepSecInStep(5);

            checkIfFlowDeploymentNotExists(mxeCliDriver, flowDeploymentName);

            executeInStep("Delete flow '" + flowName + "'", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).deleteFlow(flowName));

            sleepSecInStep(5);

            checkIfFlowNotExists(mxeCliDriver, flowName);

        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Test MXE flow deployment scaling
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction Test MXE flow deployment scaling
     * @JcatTcAction Onboard the flow
     * @JcatTcActionResult Flow is onboarded successfully
     * @JcatTcAction List onboarded flows
     * @JcatTcActionResult Previously onboarded flow appears in the deployed flow list
     * @JcatTcAction Deploy the onboarded flow with one instance
     * @JcatTcActionResult Flow deployed successfully
     * @JcatTcAction Check if flow deployment exists with one instance
     * @JcatTcActionResult Previously deployed flow appears in the deployed flows list
     * @JcatTcAction Scale the onboarded flow to have two instances
     * @JcatTcActionResult Flow deployed scaled successfully
     * @JcatTcAction Check if flow deployment exists with two instances
     * @JcatTcActionResult Previously scaled flow exist with two instances
     * @JcatTcAction Delete flow deployment
     * @JcatTcActionResult Flow deployment deleted successfully
     * @JcatTcAction Check if flow deployment exists
     * @JcatTcActionResult Flow must not appear in the deployed flows list
     * @JcatTcAction Delete the flow
     * @JcatTcActionResult Flow deleted successfully
     * @JcatTcAction List onboarded flows
     * @JcatTcActionResult Flow must not appear in the deployed flow list
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-FLOW-DEPLOYMENT_SCALING", testTitle = "Test MXE flow deployment scaling")
    @Parameters({"flowName", "flowFile", "flowDeploymentName"})
    public void testMxeFlowDeploymentScaling(String flowName, String flowFile, String flowDeploymentName) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            final int initialInstances = 1;
            final int scaledInstances = 2;

            executeInStep("Onboard flow '" + flowName + "'", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).onboard(flowFile, flowName));

            checkIfFlowExists(mxeCliDriver, flowName);

            executeInStep("Deploy flow as '" + flowDeploymentName + "'", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).deploy(flowDeploymentName, flowName, initialInstances));

            checkIfFlowDeploymentExists(mxeCliDriver, flowDeploymentName, initialInstances);

            executeInStep("Scale flow to have " + scaledInstances + " instances", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).scaleDeployment(flowDeploymentName, scaledInstances));

            checkIfFlowDeploymentExists(mxeCliDriver, flowDeploymentName, scaledInstances);

            executeInStep("Delete flow deployment '" + flowDeploymentName + "'", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).deleteDeployment(flowDeploymentName));

            sleepSecInStep(5);

            checkIfFlowDeploymentNotExists(mxeCliDriver, flowDeploymentName);

            executeInStep("Delete flow '" + flowName + "'", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).deleteFlow(flowName));

            sleepSecInStep(5);

            checkIfFlowNotExists(mxeCliDriver, flowName);

        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Onboard and deploy a flow on local cluster: negative cases
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction Test MXE flow onboard and deploy commands negative cases
     * @JcatTcAction Onboard the flow
     * @JcatTcActionResult Flow is onboarded successfully
     * @JcatTcAction List onboarded flows
     * @JcatTcActionResult Previously onboarded flow appears in the deployed flow list
     * @JcatTcAction Onboard the already onboarded flow
     * @JcatTcActionResult Flow onboard should fail
     * @JcatTcAction Deploy the onboarded flow
     * @JcatTcActionResult Flow deployed successfully
     * @JcatTcAction Check if flow deployment exists
     * @JcatTcActionResult Previously deployed flow appears in the deployed flows list
     * @JcatTcAction Deploy the already deployed flow
     * @JcatTcActionResult Flow deploy should fail
     * @JcatTcAction Delete flow deployment
     * @JcatTcActionResult Flow deployment deleted successfully
     * @JcatTcAction Check if flow deployment exists
     * @JcatTcActionResult Flow must not appear in the deployed flows list
     * @JcatTcAction Delete a non-existing flow deployment
     * @JcatTcActionResult Deleting should fail
     * @JcatTcAction Delete the flow
     * @JcatTcActionResult Flow deleted successfully
     * @JcatTcAction List onboarded flows
     * @JcatTcActionResult Flow must not appear in the deployed flow list
     * @JcatTcAction Delete a non-existing flow
     * @JcatTcActionResult Deleting should fail
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-FLOW-ONBOARD-AND-DEPLOY-NEGATIVE-CASES",
            testTitle = "Test MXE flow onboard and deploy commands negative cases")
    @Parameters({"flowName", "flowFile", "flowDeploymentName"})
    public void testMxeFlowOnboardAndDeployNegativeCases(String flowName, String flowFile, String flowDeploymentName) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            executeInStep("Onboard flow '" + flowName + "'", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).onboard(flowFile, flowName));

            checkIfFlowExists(mxeCliDriver, flowName);

            onboardAlreadyExistingFlow(mxeCliDriver, flowName, flowFile);

            executeInStep("Deploy flow as '" + flowDeploymentName + "'", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).deploy(flowDeploymentName, flowName));

            checkIfFlowDeploymentExists(mxeCliDriver, flowDeploymentName);

            deployAlreadyExistingFlow(mxeCliDriver, flowName, flowDeploymentName);

            executeInStep("Delete flow deployment '" + flowDeploymentName + "'", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).deleteDeployment(flowDeploymentName));

            sleepSecInStep(5);

            checkIfFlowDeploymentNotExists(mxeCliDriver, flowDeploymentName);

            deleteNotExistingDeployment(mxeCliDriver, flowDeploymentName);

            executeInStep("Delete flow '" + flowName + "'", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).deleteFlow(flowName));

            sleepSecInStep(5);

            checkIfFlowNotExists(mxeCliDriver, flowName);

            deleteNotExistingFlow(mxeCliDriver, flowName);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    private void onboardAlreadyExistingFlow(MxeCliDriver mxeCliDriver, String flowName, String flowFile) {
        setTestStepBegin("Onboard already existing flow '%s'", flowName);
        CustomCommand customCommand = Commands.mxeFlow(testExecutionHost).onboard(flowFile, flowName);
        assertFailedResult(mxeCliDriver, customCommand, ".*already exists.*");
        setTestStepEnd();
    }

    private void deployAlreadyExistingFlow(MxeCliDriver mxeCliDriver, String flowName, String flowDeploymentName) {
        setTestStepBegin("Deploy already existing flow '%s'", flowDeploymentName);
        CustomCommand customCommand = Commands.mxeFlow(testExecutionHost).deploy(flowDeploymentName, flowName);
        assertFailedResult(mxeCliDriver, customCommand, ".*already exists.*");
        setTestStepEnd();
    }

    private void deleteNotExistingFlow(MxeCliDriver mxeCliDriver, String flowName) {
        setTestStepBegin("Delete not existing flow '%s'", flowName);
        CustomCommand customCommand = Commands.mxeFlow(testExecutionHost).deleteFlow(flowName);
        assertFailedResult(mxeCliDriver, customCommand, ".*does not exist.*");
        setTestStepEnd();
    }

    private void deleteNotExistingDeployment(MxeCliDriver mxeCliDriver, String flowDeploymentName) {
        setTestStepBegin("Delete not existing deployment '%s'", flowDeploymentName);
        CustomCommand customCommand = Commands.mxeFlow(testExecutionHost).deleteDeployment(flowDeploymentName);
        assertFailedResult(mxeCliDriver, customCommand, ".*does not exist.*");
        setTestStepEnd();
    }

    private void assertFailedResult(MxeCliDriver mxeCliDriver, CustomCommand customCommand,
            String expectedOutputMatcher) {
        CommandResult result = mxeCliDriver.execute(customCommand);
        assertThat(result).isNotNull();
        saveAssertThat(result.getExitCode()).as(ERROR_EXIT_CODE_ZERO).isNotEqualTo(0);
        saveAssertThat(result.getCommandOutput()).matches(Pattern.compile(expectedOutputMatcher, Pattern.DOTALL));
    }
}
