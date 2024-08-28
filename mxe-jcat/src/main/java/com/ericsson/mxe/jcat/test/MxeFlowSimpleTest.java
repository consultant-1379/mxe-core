package com.ericsson.mxe.jcat.test;

import static com.ericsson.mxe.jcat.test.MxeFlowTestHelper.checkIfFlowDeploymentExists;
import static com.ericsson.mxe.jcat.test.MxeFlowTestHelper.checkIfFlowDeploymentNotExists;
import static com.ericsson.mxe.jcat.test.MxeFlowTestHelper.checkIfFlowExists;
import static com.ericsson.mxe.jcat.test.MxeFlowTestHelper.checkIfFlowNotExists;
import static com.ericsson.mxe.jcat.test.MxeFlowTestHelper.listFlowDeploymentsInStep;
import static com.ericsson.mxe.jcat.test.MxeFlowTestHelper.listFlowsInStep;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.ERROR_RESOURCE_RELEASE;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.executeInStep;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.sleepSecInStep;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.ericsson.mxe.jcat.command.Commands;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import se.ericsson.jcat.fw.annotations.JcatClass;
import se.ericsson.jcat.fw.annotations.JcatMethod;

/**
 * @JcatDocChapterDescription Chapter covering flow function tests.
 */
@JcatClass(chapterName = "Flow Function Tests")
public class MxeFlowSimpleTest extends MxeTestBase {

    /**
     * @JcatTcDescription Onboard and deploy a flow on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction Onboard and deploy flow
     * @JcatTcAction Onboard the flow
     * @JcatTcActionResult Flow is onboarded successfully
     * @JcatTcAction List onboarded flows
     * @JcatTcActionResult Previously onboarded flow appears in the deployed flow list
     * @JcatTcAction Deploy the onboarded flow
     * @JcatTcActionResult Flow deployed successfully
     * @JcatTcAction Check if flow deployment exists
     * @JcatTcActionResult Previously deployed flow appears in the deployed flows list
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-FLOW-ONBOARD-AND-DEPLOY", testTitle = "Test MXE flow onboard and deploy commands")
    @Parameters({"flowName", "flowFile", "flowDeploymentName"})
    public void flowOnboardAndDeploy(String flowName, String flowFile, String flowDeploymentName) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            executeInStep("Onboard flow '" + flowName + "'", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).onboard(flowFile, flowName));

            checkIfFlowExists(mxeCliDriver, flowName);

            executeInStep("Deploy flow as '" + flowDeploymentName + "'", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).deploy(flowDeploymentName, flowName));

            checkIfFlowDeploymentExists(mxeCliDriver, flowDeploymentName);

        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Verify and onboarded and deployed flow on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction Check if the given flow is onboarded and deployed
     * @JcatTcAction List onboarded flows
     * @JcatTcActionResult Previously onboarded flow appears in the deployed flow list
     * @JcatTcAction Check if flow deployment exists
     * @JcatTcActionResult Previously deployed flow appears in the deployed flows list
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "VERIFY-MXE-FLOW-ONBOARD-AND-DEPLOY",
            testTitle = "Test if the MXE flow is onboarded and deployed")
    @Parameters({"flowName", "flowDeploymentName"})
    public void verifyFlowOnboardAndDeploy(String flowName, String flowDeploymentName) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            checkIfFlowExists(mxeCliDriver, flowName);

            checkIfFlowDeploymentExists(mxeCliDriver, flowDeploymentName);

        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Delete MXE Flow deployment and flow
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction Delete MXE Flow deployment and flow
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
    @JcatMethod(testTag = "TEST-MXE-DELETE-FLOW-DEPLOYMENT-AND-FLOW", testTitle = "Delete MXE Flow deployment and flow")
    @Parameters({"flowName", "flowDeploymentName"})
    public void deleteFlowDeploymentAndFlow(String flowName, String flowDeploymentName) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            listFlowsInStep(mxeCliDriver);
            listFlowDeploymentsInStep(mxeCliDriver);;

            executeInStep("Delete flow deployment '" + flowDeploymentName + "'", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).deleteDeployment(flowDeploymentName));

            listFlowsInStep(mxeCliDriver);
            listFlowDeploymentsInStep(mxeCliDriver);;

            sleepSecInStep(5);

            executeInStep("Delete flow '" + flowName + "'", mxeCliDriver,
                    () -> Commands.mxeFlow(testExecutionHost).deleteFlow(flowName));

            sleepSecInStep(5);

            checkIfFlowDeploymentNotExists(mxeCliDriver, flowDeploymentName);
            checkIfFlowNotExists(mxeCliDriver, flowName);

        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }
}
