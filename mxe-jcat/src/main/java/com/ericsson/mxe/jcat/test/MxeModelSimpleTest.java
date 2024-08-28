package com.ericsson.mxe.jcat.test;

import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.MODEL_CREATE_TIMEOUT;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.MODEL_START_TIMEOUT;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.deleteAllModelsInSubstep;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.modelListOnboardedInStep;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.modelOnboard;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.modelOnboardSource;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.waitUntilModelStatusIs;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.deleteAllServicesInSubstep;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.serviceCreateInStep;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.serviceListInStep;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.verifyServiceInstanceNumber;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.waitUntilServiceStatusIs;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.ERROR_RESOURCE_RELEASE;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.STATUS_CREATING;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.STATUS_RUNNING;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import se.ericsson.jcat.fw.annotations.JcatClass;
import se.ericsson.jcat.fw.annotations.JcatMethod;

/**
 * @JcatDocChapterDescription Chapter covering model and model service function tests.
 */
@JcatClass(chapterName = "Model Function Tests")
public class MxeModelSimpleTest extends MxeTestBase {

    private static final String PACKAGING = "packaging";

    /**
     * @JcatTcDescription Start and stop a model on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about onboarding and starting an mxe model
     * @JcatTcAction Onboard the model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Start the onboarded model
     * @JcatTcActionResult Model started successfully
     * @JcatTcAction List started models
     * @JcatTcActionResult Previously started model appears in the model list
     * @JcatTcAction Check if the model is started with the given instance number
     * @JcatTcActionResult Model is started with the given instance number
     * @JcatTcAction Using the list started models wait until the the model reaches "RUNNING" status (timeout is 5
     *               minutes)
     * @JcatTcActionResult Model reaches "RUNNING" status
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"packageName", "modelName", "modelVersion", "instance"})
    @JcatMethod(testTag = "TEST-MXE-MODEL-ONBOARD-AND-START", testTitle = "Onboard and start a model on local cluster")
    public void modelOnboardAndStart(String packageName, String modelName, String modelVersion, int instance) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            modelOnboard(mxeCliDriver, modelName, modelVersion, packageName);
            modelListOnboardedInStep(mxeCliDriver);
            String instanceName = serviceCreateInStep(mxeCliDriver, modelName, modelVersion, instance);
            serviceListInStep(mxeCliDriver);
            verifyServiceInstanceNumber(mxeCliDriver, instanceName, instance);
            waitUntilServiceStatusIs(mxeCliDriver, instanceName, STATUS_RUNNING, MODEL_START_TIMEOUT);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Create model service on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about onboarding a model and creating a model service
     * @JcatTcAction Onboard the model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Create a model service from the onboarded model
     * @JcatTcActionResult Model service created successfully
     * @JcatTcAction List services
     * @JcatTcActionResult Previously created model service appears in the model list
     * @JcatTcAction Check if the model service is created with the given instance number
     * @JcatTcActionResult Model service is created with the given instance number
     * @JcatTcAction Using the list services wait until the the model service reaches "RUNNING" status (timeout is 5
     *               minutes)
     * @JcatTcActionResult Model service reaches "RUNNING" status
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"packageName", "modelName", "modelVersion", "instance"})
    @JcatMethod(testTag = "TEST-MXE-MODEL-ONBOARD-AND-CREATE",
            testTitle = "Onboard a model and create a model on local cluster")
    public void modelOnboardAndCreate(String packageName, String modelName, String modelVersion, int instance) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            modelOnboard(mxeCliDriver, modelName, modelVersion, packageName);
            modelListOnboardedInStep(mxeCliDriver);
            String instanceName = serviceCreateInStep(mxeCliDriver, modelName, modelVersion, instance);
            serviceListInStep(mxeCliDriver);
            verifyServiceInstanceNumber(mxeCliDriver, instanceName, instance);
            waitUntilServiceStatusIs(mxeCliDriver, instanceName, STATUS_RUNNING, MODEL_START_TIMEOUT);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Verify model service is running on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about verifying an mxe model service
     * @JcatTcAction List services
     * @JcatTcActionResult Previously created model service appears in the model list
     * @JcatTcAction Check if the model service is created with the given instance number
     * @JcatTcActionResult Model service is created with the given instance number
     * @JcatTcAction Using the list services wait until the the model reaches "RUNNING" status (timeout is 5 minutes)
     * @JcatTcActionResult Model service reaches "RUNNING" status
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"instanceName", "instance"})
    @JcatMethod(testTag = "TEST-eric-mxe-model-service-VERIFY",
            testTitle = "Verify a model service is running on local cluster")
    public void verifyModelServiceIsRunning(String instanceName, int instance) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            serviceListInStep(mxeCliDriver);
            verifyServiceInstanceNumber(mxeCliDriver, instanceName, instance);
            waitUntilServiceStatusIs(mxeCliDriver, instanceName, STATUS_RUNNING, MODEL_START_TIMEOUT);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Onboard a model from source on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about onboard an mxe model from source
     * @JcatTcAction Onboard the model from source
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models and wait for packaging status
     * @JcatTcActionResult Previously onboarded model appears in the model list as packaging
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"modelSourcePath", "sourceModelName", "modelVersion"})
    @JcatMethod(testTag = "TEST-MXE-ONBOARDMODELFROMSOURCE", testTitle = "Model onboarding from source")
    public void onboardModelFromSource(String modelSourcePath, String sourceModelName, String modelVersion) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            modelOnboardSource(mxeCliDriver, sourceModelName, modelSourcePath);
            waitUntilModelStatusIs(mxeCliDriver, sourceModelName, modelVersion, PACKAGING, MODEL_CREATE_TIMEOUT);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Create a model service on local cluster with previously onboarded model
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about creating an mxe model service
     * @JcatTcAction Create a model service with an onboarded model
     * @JcatTcActionResult Model service created successfully
     * @JcatTcAction List model services and wait for creating status
     * @JcatTcActionResult Previously created model service appears in the model list with 'creating' status
     * @JcatTcAction List model services and wait for started status
     * @JcatTcActionResult Previously created model service appears in the model list with 'running' status
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"modelName", "modelVersion"})
    @JcatMethod(testTag = "TEST-MXE-START-MODEL",
            testTitle = "Create a model service on local cluster with previously onboarded model")
    public void createModelService(String modelName, String modelVersion) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            modelListOnboardedInStep(mxeCliDriver);
            String instanceName = serviceCreateInStep(mxeCliDriver, modelName, modelVersion, 1);
            waitUntilServiceStatusIs(mxeCliDriver, instanceName, STATUS_CREATING, MODEL_CREATE_TIMEOUT);
            waitUntilServiceStatusIs(mxeCliDriver, instanceName, STATUS_RUNNING, MODEL_START_TIMEOUT);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Delete all model services and models on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about stopping and deleting all mxe model
     * @JcatTcAction Delete all model services on local cluster
     * @JcatTcActionResult All model services are deleted
     * @JcatTcAction Delete all models on local cluster
     * @JcatTcActionResult All models are deleted
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-MODEL-CLEANUP", testTitle = "Delete all model services and models on local cluster")
    public void cleanupAllModels() {
        deleteAllServicesInSubstep(testExecutionHost);
        deleteAllModelsInSubstep(testExecutionHost);
        MxeTestHelper.sleepSec(10);
    }
}
