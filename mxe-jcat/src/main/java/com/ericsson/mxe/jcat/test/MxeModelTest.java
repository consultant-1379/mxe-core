package com.ericsson.mxe.jcat.test;

import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.MODEL_CREATE_TIMEOUT;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.MODEL_ONBOARD_TIMEOUT;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.MODEL_START_TIMEOUT;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.deleteAllModelsInSubstep;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.getTemporalModelId;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.modelDelete;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.modelDeleteFailed;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.modelListOnboardedInStep;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.modelOnboard;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.modelOnboardArchive;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.modelOnboardArchiveExpectFail;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.modelOnboardSource;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.modelOnboardSourceExpectFail;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.waitUntilModelStatusIs;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.deleteAllServicesInSubstep;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.serviceCreateInStep;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.serviceDelete;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.waitUntilServiceStatusIs;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.ERROR_RESOURCE_RELEASE;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.STATUS_AVAILABLE;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.STATUS_CREATING;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.STATUS_RUNNING;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.UNKNOWN;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.executeInStep;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.sleepSec;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.ericsson.mxe.jcat.command.Commands;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import se.ericsson.jcat.fw.annotations.JcatClass;
import se.ericsson.jcat.fw.annotations.JcatMethod;
import se.ericsson.jcat.fw.annotations.Teardown;
import se.ericsson.jcat.fw.model.JcatModelHolder;

/**
 * @JcatDocChapterDescription Chapter covering model function tests.
 */
@JcatClass(chapterName = "Model Function Tests")
public class MxeModelTest extends MxeTestBase {

    /**
     * @JcatTcDescription List MXE model commands
     * @JcatTcPreconditions None
     * @JcatTcInstruction The testcase is about running the mxe-model CLI to print list related help messages using
     *                    default cluster
     * @JcatTcAction mxe-model list --help --verbose
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction mxe-model list --verbose
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction mxe-model list --help
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction mxe-model list
     * @JcatTcActionResult Help message is printed
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-MODEL-COMMANDS", testTitle = "List MXE model commands")
    public void testMxeModelPrintHelp() {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            executeInStep("Model list help verbose", mxeCliDriver,
                    () -> Commands.mxeModel(testExecutionHost).list().help().verbose());
            executeInStep("Model list verbose", mxeCliDriver,
                    () -> Commands.mxeModel(testExecutionHost).list().verbose());
            executeInStep("Model list help", mxeCliDriver, () -> Commands.mxeModel(testExecutionHost).list().help());
            executeInStep("Model list", mxeCliDriver, () -> Commands.mxeModel(testExecutionHost).list());
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Delete a model on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about deleting an mxe model
     * @JcatTcAction Onboard the model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Start the onboarded model
     * @JcatTcActionResult Model started successfully
     * @JcatTcAction List started models
     * @JcatTcActionResult Previously started model appears in the model list
     * @JcatTcAction Try to delete already started model
     * @JcatTcActionResult Model delete failed
     * @JcatTcAction Stop started model
     * @JcatTcActionResult Model stopped successfully
     * @JcatTcAction Try to delete a non existing model
     * @JcatTcActionResult Model delete failed
     * @JcatTcAction Delete model
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"packageName", "modelName", "modelVersion"})
    @JcatMethod(testTag = "TEST-MXE-MODELDELETE", testTitle = "Model delete tests")
    public void testMxeModelOnboardDelete(String packageName, String modelName, String modelVersion) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            modelOnboard(mxeCliDriver, modelName, modelVersion, packageName);
            modelListOnboardedInStep(mxeCliDriver);
            modelDeleteFailed(mxeCliDriver, modelName, modelVersion);
            modelDelete(mxeCliDriver, modelName, modelVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Onboard a model from source on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about deleting an mxe model
     * @JcatTcAction Onboard a faulty model from source
     * @JcatTcActionResult Model onboard failed, because there is no INFO file in it
     * @JcatTcAction Onboard the model from source
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models and wait for available status
     * @JcatTcActionResult Previously onboarded model appears in the model list as available
     * @JcatTcAction Start the onboarded model
     * @JcatTcActionResult Model started successfully
     * @JcatTcAction List started models and wait for started status
     * @JcatTcActionResult Previously started model appears in the model list
     * @JcatTcAction Stop started model
     * @JcatTcActionResult Model stopped successfully
     * @JcatTcAction Delete model
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"modelSourcePath", "faultyModelSourcePath", "sourceModelName", "modelVersion"})
    @JcatMethod(testTag = "TEST-MXE-MODELONBOARDSOURCE", testTitle = "Model onboarding from source")
    public void testMxeModelOnboardFromSource(String modelSourcePath, String faultyModelSourcePath,
            String sourceModelName, String modelVersion) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            modelOnboardSourceExpectFail(mxeCliDriver, sourceModelName, faultyModelSourcePath);
            modelOnboardSource(mxeCliDriver, sourceModelName, modelSourcePath);
            waitUntilModelStatusIs(mxeCliDriver, sourceModelName, modelVersion, STATUS_AVAILABLE,
                    MODEL_ONBOARD_TIMEOUT);
            sleepSec(120);
            String instanceName = serviceCreateInStep(mxeCliDriver, sourceModelName, modelVersion, 1);
            waitUntilServiceStatusIs(mxeCliDriver, instanceName, STATUS_CREATING, MODEL_CREATE_TIMEOUT);
            waitUntilServiceStatusIs(mxeCliDriver, instanceName, STATUS_RUNNING, MODEL_START_TIMEOUT);
            serviceDelete(mxeCliDriver, instanceName);
            modelDelete(mxeCliDriver, sourceModelName, modelVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Onboard a model from archive on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about onboarding an mxe model
     * @JcatTcAction Onboard the model from archive
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models and wait for available status
     * @JcatTcActionResult Previously onboarded model appears in the model list as available
     * @JcatTcAction Onboard a faulty model from archive
     * @JcatTcActionResult Model onboard failed, because there is no model in the archive
     * @JcatTcAction Delete the faulty model
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcAction Delete model
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"packageName", "modelName", "modelVersion", "wrongModelName"})
    @JcatMethod(testTag = "TEST-MXE-MODELONBOARDARCHIVE", testTitle = "Model onboarding from archive")
    public void testMxeModelOnboardFromArchive(String packageName, String modelName, String modelVersion,
            String wrongModelName) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            modelOnboardArchive(mxeCliDriver, modelName, modelVersion, packageName);
            modelListOnboardedInStep(mxeCliDriver);
            modelOnboardArchiveExpectFail(mxeCliDriver, wrongModelName);
            modelDelete(mxeCliDriver, getTemporalModelId(mxeCliDriver, wrongModelName), UNKNOWN);
            modelDelete(mxeCliDriver, modelName, modelVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    @Teardown(alwaysRun = true)
    public void cleanup() {
        switch (JcatModelHolder.getCurrentTestCase().getTestResult()) {
            case ERROR:
            case FAILED:
            case INCONCLUSIVE: {
                setTestStepBegin("Cleanup");
                deleteAllServicesInSubstep(testExecutionHost);
                deleteAllModelsInSubstep(testExecutionHost);
                setTestStepEnd();
            }
            default: {
                // NOOP
            }
        }
    }
}
