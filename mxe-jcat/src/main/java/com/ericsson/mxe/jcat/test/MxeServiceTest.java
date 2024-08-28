package com.ericsson.mxe.jcat.test;

import com.ericsson.mxe.jcat.command.Commands;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import com.ericsson.mxe.jcat.dto.MxeModel;
import com.ericsson.mxe.jcat.dto.autoscaling.AutoscalingMetric;
import com.ericsson.mxe.jcat.dto.autoscaling.AutoscalingMetricName;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.JcatClass;
import se.ericsson.jcat.fw.annotations.JcatMethod;
import se.ericsson.jcat.fw.annotations.Teardown;
import se.ericsson.jcat.fw.model.JcatModelHolder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.*;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.*;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.*;

/**
 * @JcatDocChapterDescription Chapter covering model service function tests.
 */
@JcatClass(chapterName = "Service Function Tests")
public class MxeServiceTest extends MxeTestBase {

    private static final Duration SERVICE_CREATE_TIMEOUT = Duration.ofMinutes(15);
    private static final long SLEEP_TIME_SEC_AFTER_MODIFY = 1;
    private static final String MXE_MODEL_SERVICE_NAME = "test-model-service";
    private static final String MXE_MODEL_SERVICE_NAME_SECOND = "test-model-service-second";
    private static final AutoscalingMetric AUTOSCALE_CPU_METRIC_TEST_VALUE =
            new AutoscalingMetric(AutoscalingMetricName.CPU_MILLI_CORES, 10);
    private static final AutoscalingMetric AUTOSCALE_MEMORY_METRIC_TEST_VALUE =
            new AutoscalingMetric(AutoscalingMetricName.MEMORY_MEGA_BYTES, 20);


    /**
     * @JcatTcDescription List MXE service commands
     * @JcatTcPreconditions None
     * @JcatTcInstruction The testcase is about running the model-service CLI to print list related help messages using
     *                    default cluster
     * @JcatTcAction model-service list --help --verbose
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction MXE-SINGLE-MODEL-SERVICE list --verbose
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction MXE-SINGLE-MODEL-SERVICE list --help
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction MXE-SINGLE-MODEL-SERVICE list
     * @JcatTcActionResult Help message is printed
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-SINGLE-MODEL-SERVICE-COMMANDS", testTitle = "List MXE model commands")
    public void testMxeModelServicePrintHelp() {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            executeInStep("Service list help verbose", mxeCliDriver,
                    () -> Commands.mxeService(testExecutionHost).list().help().verbose());
            executeInStep("Service list verbose", mxeCliDriver,
                    () -> Commands.mxeService(testExecutionHost).list().verbose());
            executeInStep("Service list help", mxeCliDriver,
                    () -> Commands.mxeService(testExecutionHost).list().help());
            executeInStep("Service list", mxeCliDriver, () -> Commands.mxeService(testExecutionHost).list());
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Create and delete a model service on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about onboarding a model and creating an mxe model service
     * @JcatTcAction Onboard the model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Create model service from the onboarded model
     * @JcatTcActionResult Model service created successfully
     * @JcatTcAction List services
     * @JcatTcActionResult Previously created model service appears in the service list
     * @JcatTcAction Check if the service is created with the given model
     * @JcatTcActionResult Service is created with the given model
     * @JcatTcAction Check if the service is created with the given instance number
     * @JcatTcActionResult Service is created with the given instance number
     * @JcatTcAction Using the list services wait until the the model service reaches "RUNNING" status (timeout is 5
     *               minutes)
     * @JcatTcActionResult Model service reaches "RUNNING" status
     * @JcatTcAction Create model service from the onboarded model with different name
     * @JcatTcActionResult Model service created successfully with different name
     * @JcatTcAction List services
     * @JcatTcActionResult Previously created model service appears in the service list
     * @JcatTcAction Check if the service is created with the given model
     * @JcatTcActionResult Service is created with the given model
     * @JcatTcAction Check if the service is created with the given instance number
     * @JcatTcActionResult Service is created with the given instance number
     * @JcatTcAction Using the list services wait until the the model service reaches "RUNNING" status (timeout is 5
     *               minutes)
     * @JcatTcActionResult Model service reaches "RUNNING" status
     * @JcatTcAction Delete created model services
     * @JcatTcActionResult Model services deleted successfully
     * @JcatTcAction Delete model
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"packageName", "modelId", "modelVersion", "instance"})
    @JcatMethod(testTag = "TEST-MXE-SINGLE-MODEL-SERVICE-CREATE",
            testTitle = "Create and delete a model on local cluster")
    public void testMxeSingleModelServiceCreate(String packageName, String modelId, String modelVersion, int instance) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            modelOnboard(mxeCliDriver, modelId, modelVersion, packageName);
            modelListOnboardedInStep(mxeCliDriver);
            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion, instance);
            serviceListInStep(mxeCliDriver);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, instance);
            verifyServiceDomain(mxeCliDriver, MXE_MODEL_SERVICE_NAME, "");
            waitUntilServiceStatusIs(mxeCliDriver, MXE_MODEL_SERVICE_NAME, STATUS_RUNNING, SERVICE_CREATE_TIMEOUT);
            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME_SECOND, modelId, modelVersion, instance);
            serviceListInStep(mxeCliDriver);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME_SECOND, modelId, modelVersion);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME_SECOND, instance);
            verifyServiceDomain(mxeCliDriver, MXE_MODEL_SERVICE_NAME, "");
            waitUntilServiceStatusIs(mxeCliDriver, MXE_MODEL_SERVICE_NAME_SECOND, STATUS_RUNNING,
                    SERVICE_CREATE_TIMEOUT);
            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME_SECOND);
            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME);
            modelDelete(mxeCliDriver, modelId, modelVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Create and delete a model service on local cluster: negative cases
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about testing the negative cases of onboarding and deleting a model, creating
     *                    and deleting an mxe model service
     * @JcatTcAction Onboard the model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction Onboard the model again from the same docker image
     * @JcatTcActionResult Model onboard should fail
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Create a model service from the onboarded model
     * @JcatTcActionResult Model service created successfully
     * @JcatTcAction List model services
     * @JcatTcActionResult Previously created model service appears in the service list
     * @JcatTcAction Check if the model service is created with the given instance number
     * @JcatTcActionResult Model service is created with the given instance number
     * @JcatTcAction Check if the model service is created with the given model
     * @JcatTcActionResult Model service is created with the given model
     * @JcatTcAction Create a model service with already existing name
     * @JcatTcActionResult Model service create should fail
     * @JcatTcAction Create a model service from a non-existing model
     * @JcatTcActionResult Model service create should fail
     * @JcatTcAction Delete created model service
     * @JcatTcActionResult Model service deleted successfully
     * @JcatTcAction Delete a model service with non-existing name
     * @JcatTcActionResult Model service delete should fail
     * @JcatTcAction Delete model
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcAction Delete a non-existing model
     * @JcatTcActionResult Model deleted should fail
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"packageName", "modelId", "modelVersion", "instance"})
    @JcatMethod(testTag = "TEST-MXE-SINGLE-MODEL-SERVICE-CREATE-NEGATIVE-CASES",
            testTitle = "Create and delete a model service on local cluster: negative cases")
    public void testMxeSingleModelServiceCreateNegativeCases(String packageName, String modelId, String modelVersion,
            int instance) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            modelOnboard(mxeCliDriver, modelId, modelVersion, packageName);
            modelOnboardAlreadyOnboardedExpectFail(mxeCliDriver, modelId, modelVersion, packageName);
            modelListOnboardedInStep(mxeCliDriver);
            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion, instance);
            serviceListInStep(mxeCliDriver);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, instance);
            serviceCreateAlreadyCreatedExpectFail(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion,
                    instance);
            serviceCreateSingleServiceNonExistingModelExpectFail(mxeCliDriver);
            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME);
            serviceDeleteNonExistingModelExpectFail(mxeCliDriver);
            modelDelete(mxeCliDriver, modelId, modelVersion);
            modelDeleteNonExistingModelExpectFail(mxeCliDriver);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Modify instances of a model service
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about onboarding a model, creating a model service and modifying the instances
     * @JcatTcAction Onboard the model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Create a model service from onboarded model with initial instance number
     * @JcatTcActionResult Service created successfully
     * @JcatTcAction List model services
     * @JcatTcActionResult Previously created model service appears in the service list
     * @JcatTcAction Check if service endpoint appears in service list
     * @JcatTcActionResult Service endpoint appears in service list
     * @JcatTcAction Check if the service is created with the initial instance number
     * @JcatTcActionResult Service is created with the initial instance number
     * @JcatTcAction Modify instances of the model service to initial instance number
     * @JcatTcActionResult Model service instances won`t change
     * @JcatTcAction Check if the model service is running with initial instance number
     * @JcatTcActionResult Model service is running with the initial instance number
     * @JcatTcAction Modify instances of the model service (scale out)
     * @JcatTcActionResult Model service scaled out successfully
     * @JcatTcAction Check if the model service is running with the scaled-out instance number
     * @JcatTcActionResult Model service is running with the scaled-out instance number
     * @JcatTcAction Modify instances of the model service to the initial instance number (scale-in)
     * @JcatTcActionResult Model service scaled in successfully
     * @JcatTcAction Check if the model service is running with the initial instance number
     * @JcatTcActionResult Model service is running with the initial instance number
     * @JcatTcAction Modify instances of a non-existing model service
     * @JcatTcActionResult Model service modify should fail
     * @JcatTcAction Modify instances of a model service to a negative instance number
     * @JcatTcActionResult Model service modify should fail
     * @JcatTcAction Check if the model service is running with the initial instance number
     * @JcatTcActionResult Model service is running with the initial instance number
     * @JcatTcAction Delete the created model service
     * @JcatTcActionResult Model service deleted successfully
     * @JcatTcAction Delete model
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"packageName", "modelId", "modelVersion"})
    @JcatMethod(testTag = "TEST-MXE-SINGLE-MODEL-SERVICE-SCALE", testTitle = "Scaling a model service")
    public void testMxeSingleModelServiceScale(String packageName, String modelId, String modelVersion) {
        final int initialInstanceNumber = 1;
        final int scaleInstanceNumber = 2;
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            // Onboard a model and create a test model service
            modelOnboard(mxeCliDriver, modelId, modelVersion, packageName);
            modelListOnboardedInStep(mxeCliDriver);
            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion, initialInstanceNumber);
            serviceListInStep(mxeCliDriver);
            verifyServiceEndpoint(mxeCliDriver, MXE_MODEL_SERVICE_NAME);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, initialInstanceNumber);

            // Testing modify to the same instance number
            serviceModifyInstances(mxeCliDriver, MXE_MODEL_SERVICE_NAME, initialInstanceNumber, initialInstanceNumber);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, initialInstanceNumber);
            waitAfterServiceModify(mxeCliDriver);

            // Testing scale-out
            serviceModifyInstances(mxeCliDriver, MXE_MODEL_SERVICE_NAME, initialInstanceNumber, scaleInstanceNumber);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, scaleInstanceNumber);
            waitAfterServiceModify(mxeCliDriver);

            // Testing scale-in
            serviceModifyInstances(mxeCliDriver, MXE_MODEL_SERVICE_NAME, scaleInstanceNumber, initialInstanceNumber);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, initialInstanceNumber);
            waitAfterServiceModify(mxeCliDriver);

            // Negative test of scaling, unknown model
            serviceModifyInstancesFailed(mxeCliDriver);

            // Testcase cleanup
            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME);
            modelDelete(mxeCliDriver, modelId, modelVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }


    /**
     * @JcatTcDescription Upgrading a model service
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about upgrading an mxe model
     * @JcatTcAction Onboard a model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Onboard the target model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Create a model service with the first model
     * @JcatTcActionResult Model service created successfully
     * @JcatTcAction Check if the model service is created with first model
     * @JcatTcActionResult Previously created model service is running with required model
     * @JcatTcAction Modify service to an unknown model
     * @JcatTcActionResult Model service modify should fail
     * @JcatTcAction Modify the model service to the current model
     * @JcatTcActionResult Model service modify should fail
     * @JcatTcAction Check if the model service is running with the initial model
     * @JcatTcActionResult Model service is running with the initial model
     * @JcatTcAction Modify the service to the target model
     * @JcatTcActionResult Model service modified successfully
     * @JcatTcAction Check if the model service is running with the target model
     * @JcatTcActionResult Model service is running with the target model
     * @JcatTcAction Modify the service to the initial model
     * @JcatTcActionResult Model service modified successfully
     * @JcatTcAction Check if the model service is running with the initial model
     * @JcatTcActionResult Model service is running with the initial model
     * @JcatTcAction Modify the service to the initial model and initial instance
     * @JcatTcActionResult Model service modify is successful with nothing to change message
     * @JcatTcAction Check if the model service is running with the initial model
     * @JcatTcActionResult Model service is running with the initial model
     * @JcatTcAction Check if the model service is running with the initial instance number
     * @JcatTcActionResult Model service is running with the initial instance number
     * @JcatTcAction Modify the service to the target model and target instance
     * @JcatTcActionResult Model service modified successfully
     * @JcatTcAction Check if the model service is running with the target model
     * @JcatTcActionResult Model service is running with the target model
     * @JcatTcAction Check if the model service is running with the scaled-out instance number
     * @JcatTcActionResult Model service is running with the scaled-out instance number
     * @JcatTcAction Delete the created model service
     * @JcatTcActionResult Model service deleted successfully
     * @JcatTcAction Delete models
     * @JcatTcActionResult Models deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"packageName", "targetPackageName", "modelId", "modelVersion", "targetModelId", "targetModelVersion"})
    @JcatMethod(testTag = "TEST-MXE-SINGLE-MODEL-SERVICE-MODIFY", testTitle = "Modifying a model service")
    @SuppressWarnings("squid:S00107")
    public void testMxeSingleModelServiceModify(String packageName, String targetPackageName, String modelId,
            String modelVersion, String targetModelId, String targetModelVersion) {
        final int initialInstanceNumber = 1;
        final int scaleInstanceNumber = 2;
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            // Onboard a model and create a test model service
            modelOnboard(mxeCliDriver, modelId, modelVersion, packageName);
            modelOnboard(mxeCliDriver, targetModelId, targetModelVersion, targetPackageName);
            modelListOnboardedInStep(mxeCliDriver);
            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion, initialInstanceNumber);
            serviceListInStep(mxeCliDriver);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, initialInstanceNumber);

            // Testing modify service to existing model, unknown service, the service to an unknown model
            serviceModifyModelFailed(mxeCliDriver, MXE_MODEL_SERVICE_NAME);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion);
            waitAfterServiceModify(mxeCliDriver);

            // Testing modify model in a service
            serviceModifyModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, targetModelId, targetModelVersion, true);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, targetModelId, targetModelVersion);
            waitAfterServiceModify(mxeCliDriver);

            // Testing modify model in a service to the initial model
            serviceModifyModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion, true);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion);
            waitAfterServiceModify(mxeCliDriver);

            // Testing modify model in a service to the actual model
            serviceModifyModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion, false);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion);
            waitAfterServiceModify(mxeCliDriver);

            // Testing modify model and instances in a service to the actual
            serviceModifyInstancesModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion,
                    initialInstanceNumber, false);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, initialInstanceNumber);
            waitAfterServiceModify(mxeCliDriver);

            // Testing modify model and instances in a service
            serviceModifyInstancesModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, targetModelId, targetModelVersion,
                    scaleInstanceNumber, true);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, targetModelId, targetModelVersion);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, scaleInstanceNumber);
            waitAfterServiceModify(mxeCliDriver);

            // Testcase cleanup
            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME);
            modelDelete(mxeCliDriver, modelId, modelVersion);
            modelDelete(mxeCliDriver, targetModelId, targetModelVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }


    /**
     * @JcatTcDescription Create and delete a model service with autoscaling on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about onboarding a model and creating an mxe model service
     * @JcatTcAction Onboard the model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Create model service from the onboarded model with autoscaling configuration
     * @JcatTcActionResult Model service created successfully
     * @JcatTcAction List services
     * @JcatTcActionResult Previously created model service appears in the service list
     * @JcatTcAction Check if the service is created with the given model
     * @JcatTcActionResult Service is created with the given model
     * @JcatTcAction Check if the service is created with the given min and max instance number
     * @JcatTcActionResult Service is created with the given min and max instance number
     * @JcatTcAction Check if the service is created with the given autoscaling metric
     * @JcatTcActionResult Service is created with with the given autoscaling metric
     * @JcatTcAction Using the list services wait until the the model service reaches "RUNNING" status (timeout is 5
     *               minutes)
     * @JcatTcActionResult Model service reaches "RUNNING" status
     * @JcatTcAction Create model service from the onboarded model with different name
     * @JcatTcActionResult Model service created successfully with different name
     * @JcatTcAction List services
     * @JcatTcActionResult Previously created model service appears in the service list
     * @JcatTcAction Check if the service is created with the given model
     * @JcatTcActionResult Service is created with the given model
     * @JcatTcAction Check if the service is created with the given min and max instance number
     * @JcatTcActionResult Service is created with the given min and max instance number
     * @JcatTcAction Check if the service is created with the given autoscaling metric
     * @JcatTcActionResult Service is created with with the given autoscaling metric
     * @JcatTcAction Using the list services wait until the the model service reaches "RUNNING" status (timeout is 5
     *               minutes)
     * @JcatTcActionResult Model service reaches "RUNNING" status
     * @JcatTcAction Delete created model services
     * @JcatTcActionResult Model services deleted successfully
     * @JcatTcAction Delete model
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"packageName", "modelId", "modelVersion"})
    @JcatMethod(testTag = "TEST-MXE-SINGLE-AUTOSCALING-MODEL-SERVICE-CREATE",
            testTitle = "Create and delete a model on local cluster with autoscaling")
    public void testMxeSingleAutoscalingModelServiceCreate(String packageName, String modelId, String modelVersion) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            final int minReplicas = 1;
            final int maxReplicas = 3;
            String replicasString = minReplicas + "-" + maxReplicas;
            modelOnboard(mxeCliDriver, modelId, modelVersion, packageName);
            modelListOnboardedInStep(mxeCliDriver);
            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion, minReplicas, maxReplicas,
                    AUTOSCALE_CPU_METRIC_TEST_VALUE);
            serviceListInStep(mxeCliDriver);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, replicasString);
            verifyServiceAutoScalingMetric(mxeCliDriver, MXE_MODEL_SERVICE_NAME, AUTOSCALE_CPU_METRIC_TEST_VALUE);
            waitUntilServiceStatusIs(mxeCliDriver, MXE_MODEL_SERVICE_NAME, STATUS_RUNNING, SERVICE_CREATE_TIMEOUT);

            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME_SECOND, modelId, modelVersion, minReplicas,
                    maxReplicas, AUTOSCALE_MEMORY_METRIC_TEST_VALUE);
            serviceListInStep(mxeCliDriver);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME_SECOND, modelId, modelVersion);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME_SECOND, replicasString);
            verifyServiceAutoScalingMetric(mxeCliDriver, MXE_MODEL_SERVICE_NAME_SECOND,
                    AUTOSCALE_MEMORY_METRIC_TEST_VALUE);
            waitUntilServiceStatusIs(mxeCliDriver, MXE_MODEL_SERVICE_NAME_SECOND, STATUS_RUNNING,
                    SERVICE_CREATE_TIMEOUT);

            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME_SECOND);
            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME);
            modelDelete(mxeCliDriver, modelId, modelVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Modify the autoscaling method of a model service
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about onboarding a model, creating a model service and modifying the instances
     * @JcatTcAction Onboard the model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Create a model service from onboarded model with autoscaling
     * @JcatTcActionResult Service created successfully
     * @JcatTcAction List model services
     * @JcatTcActionResult Previously created model service appears in the service list
     * @JcatTcAction Check if service endpoint appears in service list
     * @JcatTcActionResult Service endpoint appears in service list
     * @JcatTcAction Check if the service is created with the given min and max instance number
     * @JcatTcActionResult Service is created with the given min and max instance number
     * @JcatTcAction Check if the service is created with the given autoscaling metric
     * @JcatTcActionResult Service is created with with the given autoscaling metric
     * @JcatTcAction Modify scaling parameters to the initial one
     * @JcatTcActionResult Model service autoscaling won`t change
     * @JcatTcAction Check if the service is running with the given min and max instance number
     * @JcatTcActionResult Service is running with the given min and max instance number
     * @JcatTcAction Check if the service is running with the given autoscaling metric
     * @JcatTcActionResult Service is running with the given autoscaling metric
     * @JcatTcAction Modify scaling parameters to use different metric and higher maxReplica
     * @JcatTcActionResult Model service autoscaling is modified
     * @JcatTcAction Check if the service is running with the given min and max instance number
     * @JcatTcActionResult Service is running with the given min and max instance number
     * @JcatTcAction Check if the service is running with the given autoscaling metric
     * @JcatTcActionResult Service is running with the given autoscaling metric
     * @JcatTcAction Modify instances of the model to manual scaling
     * @JcatTcActionResult Model service scaled successfully
     * @JcatTcAction Check if the model service is running with the scaled-out instance number
     * @JcatTcActionResult Model service is running with the scaled-out instance number
     * @JcatTcAction Modify scaling parameters to the initial one
     * @JcatTcActionResult Model service autoscaling won`t change
     * @JcatTcAction Check if the service is running with the given min and max instance number
     * @JcatTcActionResult Service is running with the given min and max instance number
     * @JcatTcAction Check if the service is running with the given autoscaling metric
     * @JcatTcActionResult Service is running with the given autoscaling metric
     * @JcatTcAction Delete the created model service
     * @JcatTcActionResult Model service deleted successfully
     * @JcatTcAction Delete model
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"packageName", "modelId", "modelVersion"})
    @JcatMethod(testTag = "TEST-MXE-SINGLE-AUTOSCALING-MODEL-SERVICE-MODIFY-AUTOSCALING",
            testTitle = "Modify the autoscaling method of a model service")
    public void testMxeSingleAutoscalingModelServiceModifyAutoScaling(String packageName, String modelId,
            String modelVersion) {
        final int minReplicas = 1;
        final int maxReplicas = 3;
        final String replicasString = minReplicas + "-" + maxReplicas;
        final int modifiedMaxReplicas = 4;
        final String modifiedReplicasString = minReplicas + "-" + modifiedMaxReplicas;
        final int instanceNumber = 2;
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            // Onboard a model and create a test model service
            modelOnboard(mxeCliDriver, modelId, modelVersion, packageName);
            modelListOnboardedInStep(mxeCliDriver);
            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion, minReplicas, maxReplicas,
                    AUTOSCALE_CPU_METRIC_TEST_VALUE);
            serviceListInStep(mxeCliDriver);
            verifyServiceEndpoint(mxeCliDriver, MXE_MODEL_SERVICE_NAME);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, replicasString);
            verifyServiceAutoScalingMetric(mxeCliDriver, MXE_MODEL_SERVICE_NAME, AUTOSCALE_CPU_METRIC_TEST_VALUE);
            waitUntilServiceStatusIs(mxeCliDriver, MXE_MODEL_SERVICE_NAME, STATUS_RUNNING, SERVICE_CREATE_TIMEOUT);

            // Testing modify to the same autoscaling settings
            serviceModifyAutoscaling(mxeCliDriver, MXE_MODEL_SERVICE_NAME, minReplicas, maxReplicas,
                    AUTOSCALE_CPU_METRIC_TEST_VALUE, true);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, replicasString);
            verifyServiceAutoScalingMetric(mxeCliDriver, MXE_MODEL_SERVICE_NAME, AUTOSCALE_CPU_METRIC_TEST_VALUE);
            waitAfterServiceModify(mxeCliDriver);

            // Testing autoscaling modification
            serviceModifyAutoscaling(mxeCliDriver, MXE_MODEL_SERVICE_NAME, minReplicas, modifiedMaxReplicas,
                    AUTOSCALE_MEMORY_METRIC_TEST_VALUE, false);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modifiedReplicasString);
            verifyServiceAutoScalingMetric(mxeCliDriver, MXE_MODEL_SERVICE_NAME, AUTOSCALE_MEMORY_METRIC_TEST_VALUE);
            waitAfterServiceModify(mxeCliDriver);

            // Modify to instances
            serviceModifyInstances(mxeCliDriver, MXE_MODEL_SERVICE_NAME, 0, instanceNumber);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, instanceNumber);
            waitAfterServiceModify(mxeCliDriver);

            // Testing modify to the initial autoscaling settings
            serviceModifyAutoscaling(mxeCliDriver, MXE_MODEL_SERVICE_NAME, minReplicas, maxReplicas,
                    AUTOSCALE_CPU_METRIC_TEST_VALUE, false);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, replicasString);
            verifyServiceAutoScalingMetric(mxeCliDriver, MXE_MODEL_SERVICE_NAME, AUTOSCALE_CPU_METRIC_TEST_VALUE);
            waitAfterServiceModify(mxeCliDriver);

            // Testcase cleanup
            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME);
            modelDelete(mxeCliDriver, modelId, modelVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Modifying a model service which was created with autoscaling
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about modifying a model service which was created with autoscaling
     * @JcatTcAction Onboard a model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Onboard the target model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Create model service from the onboarded model with autoscaling configuration
     * @JcatTcActionResult Model service created successfully
     * @JcatTcAction List services
     * @JcatTcActionResult Previously created model service appears in the service list
     * @JcatTcAction Check if the service is created with the given model
     * @JcatTcActionResult Service is created with the given model
     * @JcatTcAction Check if the service is created with the given min and max instance number
     * @JcatTcActionResult Service is created with the given min and max instance number
     * @JcatTcAction Check if the service is created with the given autoscaling metric
     * @JcatTcActionResult Service is created with with the given autoscaling metric
     * @JcatTcAction Modify the service to the target model
     * @JcatTcActionResult Model service modified successfully
     * @JcatTcAction Check if the model service is running with the target model
     * @JcatTcActionResult Model service is running with the target model
     * @JcatTcAction Check if the service is running with the given min and max instance number
     * @JcatTcActionResult Service is running with the given min and max instance number
     * @JcatTcAction Check if the service is running with the given autoscaling metric
     * @JcatTcActionResult Service is running with with the given autoscaling metric
     * @JcatTcAction Delete the created model service
     * @JcatTcActionResult Model service deleted successfully
     * @JcatTcAction Delete models
     * @JcatTcActionResult Models deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"packageName", "targetPackageName", "modelId", "modelVersion", "targetModelId", "targetModelVersion"})
    @JcatMethod(testTag = "TEST-MXE-SINGLE-AUTOSCALING-MODEL-SERVICE-MODIFY",
            testTitle = "Modifying a model service which was created with autoscaling")
    @SuppressWarnings("squid:S00107")
    public void testMxeSingleAutoscalingModelServiceModify(String packageName, String targetPackageName, String modelId,
            String modelVersion, String targetModelId, String targetModelVersion) {
        final int minReplicas = 1;
        final int maxReplicas = 3;
        String replicasString = minReplicas + "-" + maxReplicas;
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            // Onboard a model and create a test model service
            modelOnboard(mxeCliDriver, modelId, modelVersion, packageName);
            modelOnboard(mxeCliDriver, targetModelId, targetModelVersion, targetPackageName);
            modelListOnboardedInStep(mxeCliDriver);
            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion, minReplicas, maxReplicas,
                    AUTOSCALE_CPU_METRIC_TEST_VALUE);
            serviceListInStep(mxeCliDriver);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, replicasString);
            verifyServiceAutoScalingMetric(mxeCliDriver, MXE_MODEL_SERVICE_NAME, AUTOSCALE_CPU_METRIC_TEST_VALUE);

            // Testing modify model in a service
            serviceModifyModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, targetModelId, targetModelVersion, true);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, targetModelId, targetModelVersion);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, replicasString);
            verifyServiceAutoScalingMetric(mxeCliDriver, MXE_MODEL_SERVICE_NAME, AUTOSCALE_CPU_METRIC_TEST_VALUE);
            waitAfterServiceModify(mxeCliDriver);

            // Testcase cleanup
            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME);
            modelDelete(mxeCliDriver, modelId, modelVersion);
            modelDelete(mxeCliDriver, targetModelId, targetModelVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Create and delete two model services on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about onboarding a model and creating an mxe model service
     * @JcatTcAction Onboard the first model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction Onboard the second model
     * @JcatTcActionResult Both models are onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded models appears in the model list
     * @JcatTcAction Create an AB test model service from the onboarded models
     * @JcatTcActionResult AB test model service created successfully
     * @JcatTcAction List services
     * @JcatTcActionResult Previously created model service appears in the service list
     * @JcatTcAction Check if the service is created with the given models
     * @JcatTcActionResult Service is created with the given models
     * @JcatTcAction Check if the service is created with the given instance number
     * @JcatTcActionResult Service is created with the given instance number
     * @JcatTcAction Using the list services wait until the the model service reaches "RUNNING" status (timeout is 5
     *               minutes)
     * @JcatTcActionResult Model service reaches "RUNNING" status
     * @JcatTcAction Delete created model service
     * @JcatTcActionResult Model service deleted successfully
     * @JcatTcAction Delete the first model
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcAction Delete the second model
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"packageName", "modelId", "modelVersion", "secondPackageName", "secondModelId", "secondModelVersion",
            "instance", "weight1", "weight2"})
    @JcatMethod(testTag = "TEST-MXE-AB-TEST-SERVICE-CREATE",
            testTitle = "Create and delete two models on local cluster and start AB test")
    public void testMxeABTestServiceCreate(String packageName, String modelId, String modelVersion,
            String secondPackageName, String secondModelId, String secondModelVersion, int instance, double weight1,
            double weight2) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            modelOnboard(mxeCliDriver, modelId, modelVersion, packageName);
            modelOnboard(mxeCliDriver, secondModelId, secondModelVersion, secondPackageName);
            modelListOnboardedInStep(mxeCliDriver);
            List<MxeModel> mxeModels = new ArrayList<>();
            mxeModels.add(new MxeModel(modelId, modelVersion, weight1));
            mxeModels.add(new MxeModel(secondModelId, secondModelVersion, weight2));
            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME, mxeModels, instance);
            serviceListInStep(mxeCliDriver);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, mxeModels);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, instance);
            waitUntilServiceStatusIs(mxeCliDriver, MXE_MODEL_SERVICE_NAME, STATUS_RUNNING, SERVICE_CREATE_TIMEOUT);
            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME);
            modelDelete(mxeCliDriver, modelId, modelVersion);
            modelDelete(mxeCliDriver, secondModelId, secondModelVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Create and delete two model services on local cluster: negative cases
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about testing the negative cases of onboarding and deleting two models,
     *                    creating and deleting an mxe AB test model service
     * @JcatTcAction Onboard the first model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction Onboard the second model
     * @JcatTcActionResult Both models are onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded models appears in the model list
     * @JcatTcAction Create an AB test model service from the onboarded models
     * @JcatTcActionResult AB test model service created successfully
     * @JcatTcAction List services
     * @JcatTcActionResult Previously created model service appears in the service list
     * @JcatTcAction Check if the service is created with the given models
     * @JcatTcActionResult Service is created with the given models
     * @JcatTcAction Check if the service is created with the given instance number
     * @JcatTcActionResult Service is created with the given instance number
     * @JcatTcAction Using the list services wait until the the model service reaches "RUNNING" status (timeout is 5
     *               minutes)
     * @JcatTcActionResult Model service reaches "RUNNING" status
     * @JcatTcAction Create a model service with already existing name
     * @JcatTcActionResult Model service create should fail
     * @JcatTcAction Create a model service from non-existing models
     * @JcatTcActionResult Model service create should fail
     * @JcatTcAction Delete created model service
     * @JcatTcActionResult Model service deleted successfully
     * @JcatTcAction Delete the first model
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcAction Delete the second model
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"packageName", "modelId", "modelVersion", "secondPackageName", "secondModelId", "secondModelVersion",
            "instance", "weight1", "weight2"})
    @JcatMethod(testTag = "TEST-MXE-AB-TEST-SERVICE-CREATE-NEGATIVE-CASES",
            testTitle = "Create and delete two models on local cluster and start AB test: negative cases")
    public void testMxeABTestServiceCreateNegativeCases(String packageName, String modelId, String modelVersion,
            String secondPackageName, String secondModelId, String secondModelVersion, int instance, double weight1,
            double weight2) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            modelOnboard(mxeCliDriver, modelId, modelVersion, packageName);
            modelOnboardAlreadyOnboardedExpectFail(mxeCliDriver, modelId, modelVersion, packageName);
            modelOnboard(mxeCliDriver, secondModelId, secondModelVersion, secondPackageName);
            modelListOnboardedInStep(mxeCliDriver);
            List<MxeModel> mxeModels = new ArrayList<>();
            mxeModels.add(new MxeModel(modelId, modelVersion, weight1));
            mxeModels.add(new MxeModel(secondModelId, secondModelVersion, weight2));
            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME, mxeModels, instance);
            serviceListInStep(mxeCliDriver);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, mxeModels);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, instance);
            waitUntilServiceStatusIs(mxeCliDriver, MXE_MODEL_SERVICE_NAME, STATUS_RUNNING, SERVICE_CREATE_TIMEOUT);
            serviceCreateAlreadyCreatedExpectFail(mxeCliDriver, MXE_MODEL_SERVICE_NAME, mxeModels, instance);
            serviceCreateABTestNonExistingModelExpectFail(mxeCliDriver);
            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME);
            modelDelete(mxeCliDriver, modelId, modelVersion);
            modelDelete(mxeCliDriver, secondModelId, secondModelVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Modifying an AB test model service
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about upgrading an mxe model
     * @JcatTcAction Onboard the first model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction Onboard the second model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction Onboard the third model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded models appear in the model list
     * @JcatTcAction Create an AB test model service with the first and second models
     * @JcatTcActionResult Model service created successfully
     * @JcatTcAction List services
     * @JcatTcActionResult Previously created model service appears in the service list
     * @JcatTcAction Check if the model service is created with first and second models
     * @JcatTcActionResult Previously created model service is running with required models
     * @JcatTcAction Check if the model service is created with the initial instance number
     * @JcatTcActionResult Previously created model service is running with the initial instance number
     * @JcatTcAction Modify service to the third model
     * @JcatTcActionResult Model service modified successfully
     * @JcatTcAction List services
     * @JcatTcActionResult Previously modified model service appears in the service list
     * @JcatTcAction Check if the model service is running with the third model
     * @JcatTcActionResult Model service is running with the first and third model
     * @JcatTcAction Modify the service to the scaled instance number
     * @JcatTcActionResult Model service modified successfully
     * @JcatTcAction List services
     * @JcatTcActionResult Previously modified model service appears in the service list
     * @JcatTcAction Check if the model service is running with the initial model
     * @JcatTcActionResult Model service is running with the scaled instance number
     * @JcatTcAction Modify the service to with different weights
     * @JcatTcActionResult Model service modified successfully
     * @JcatTcAction List services
     * @JcatTcActionResult Previously modified model service appears in the service list
     * @JcatTcAction Check if the model service is running with the new weights
     * @JcatTcActionResult Model service is running with the new weights
     * @JcatTcAction Modify the service to the initial model and initial instance
     * @JcatTcActionResult Model service modified successfully
     * @JcatTcAction List services
     * @JcatTcActionResult Previously modified model service appears in the service list
     * @JcatTcAction Check if the model service is running with the initial model and initial instance number
     * @JcatTcActionResult Model service is running with the initial model and initial instance number
     * @JcatTcAction Delete the created model service
     * @JcatTcActionResult Model service deleted successfully
     * @JcatTcAction Delete models
     * @JcatTcActionResult Models deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"packageName", "modelId", "modelVersion", "secondPackageName", "secondModelId", "secondModelVersion",
            "weight1", "weight2", "targetPackageName", "targetModelId", "targetModelVersion"})
    @JcatMethod(testTag = "TEST-MXE-AB-TEST-SERVICE-MODIFY", testTitle = "Modifying an ABT test service")
    @SuppressWarnings("squid:S00107")
    public void testMxeABTestServiceModify(String packageName, String modelId, String modelVersion,
            String secondPackageName, String secondModelId, String secondModelVersion, double weight1, double weight2,
            String targetPackageName, String targetModelId, String targetModelVersion) {
        final int initialInstanceNumber = 1;
        final int scaleInstanceNumber = 2;
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            modelOnboard(mxeCliDriver, modelId, modelVersion, packageName);
            modelOnboard(mxeCliDriver, secondModelId, secondModelVersion, secondPackageName);
            modelOnboard(mxeCliDriver, targetModelId, targetModelVersion, targetPackageName);
            modelListOnboardedInStep(mxeCliDriver);
            List<MxeModel> mxeModels = new ArrayList<>();
            mxeModels.add(new MxeModel(modelId, modelVersion, weight1));
            mxeModels.add(new MxeModel(secondModelId, secondModelVersion, weight2));
            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME, mxeModels, initialInstanceNumber);
            serviceListInStep(mxeCliDriver);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, mxeModels);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, initialInstanceNumber);
            waitUntilServiceStatusIs(mxeCliDriver, MXE_MODEL_SERVICE_NAME, STATUS_RUNNING, SERVICE_CREATE_TIMEOUT);

            List<MxeModel> targetMxeModels = new ArrayList<>();
            targetMxeModels.add(new MxeModel(modelId, modelVersion));
            targetMxeModels.add(new MxeModel(targetModelId, targetModelVersion));
            serviceModifyModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, targetMxeModels, true);
            serviceListInStep(mxeCliDriver);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, targetMxeModels);
            waitAfterServiceModify(mxeCliDriver);

            serviceModifyModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, scaleInstanceNumber, true);
            serviceListInStep(mxeCliDriver);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, scaleInstanceNumber);
            waitAfterServiceModify(mxeCliDriver);

            targetMxeModels = new ArrayList<>();
            targetMxeModels.add(new MxeModel(0.3));
            targetMxeModels.add(new MxeModel(0.7));
            serviceModifyModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, targetMxeModels, true);
            serviceListInStep(mxeCliDriver);
            targetMxeModels = new ArrayList<>();
            targetMxeModels.add(new MxeModel(modelId, modelVersion, 0.3));
            targetMxeModels.add(new MxeModel(targetModelId, targetModelVersion, 0.7));
            verifyServiceModelWeights(mxeCliDriver, MXE_MODEL_SERVICE_NAME, targetMxeModels);
            waitAfterServiceModify(mxeCliDriver);

            targetMxeModels = new ArrayList<>();
            targetMxeModels.add(new MxeModel(modelId, modelVersion));
            targetMxeModels.add(new MxeModel(secondModelId, secondModelVersion));
            serviceModifyModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, targetMxeModels, initialInstanceNumber, true);
            serviceListInStep(mxeCliDriver);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, targetMxeModels);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, initialInstanceNumber);
            waitAfterServiceModify(mxeCliDriver);

            serviceModifyModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, targetMxeModels, initialInstanceNumber, false);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, targetMxeModels);
            waitAfterServiceModify(mxeCliDriver);

            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME);
            modelDelete(mxeCliDriver, modelId, modelVersion);
            modelDelete(mxeCliDriver, secondModelId, secondModelVersion);
            modelDelete(mxeCliDriver, targetModelId, targetModelVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Modifying an AB test model service: negative cases
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about upgrading an mxe model
     * @JcatTcAction Onboard the first model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction Onboard the second model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded models appear in the model list
     * @JcatTcAction Create an AB test model service with the first and second models
     * @JcatTcActionResult Model service created successfully
     * @JcatTcAction List services
     * @JcatTcActionResult Previously created model service appears in the service list
     * @JcatTcAction Check if the model service is created with first and second models
     * @JcatTcActionResult Previously created model service is running with required models
     * @JcatTcAction Check if the model service is created with the initial instance number
     * @JcatTcActionResult Previously created model service is running with the initial instance number
     * @JcatTcAction Modify service to a non existing model
     * @JcatTcActionResult Model service modify should fail
     * @JcatTcAction Modify a non existing model service
     * @JcatTcActionResult Model service modify should fail
     * @JcatTcAction Modify model service with one model
     * @JcatTcActionResult Model service modify should fail
     * @JcatTcAction Delete the created model service
     * @JcatTcActionResult Model service deleted successfully
     * @JcatTcAction Delete models
     * @JcatTcActionResult Models deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"packageName", "modelId", "modelVersion", "secondPackageName", "secondModelId", "secondModelVersion",
            "weight1", "weight2"})
    @JcatMethod(testTag = "TEST-MXE-AB-TEST-SERVICE-MODIFY-NEGATIVE-CASES",
            testTitle = "Modifying an ABT test service: negative cases")
    @SuppressWarnings("squid:S00107")
    public void testMxeABTestServiceModifyNegativeCases(String packageName, String modelId, String modelVersion,
            String secondPackageName, String secondModelId, String secondModelVersion, double weight1, double weight2) {
        final int initialInstanceNumber = 1;
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            modelOnboard(mxeCliDriver, modelId, modelVersion, packageName);
            modelOnboard(mxeCliDriver, secondModelId, secondModelVersion, secondPackageName);
            modelListOnboardedInStep(mxeCliDriver);
            List<MxeModel> mxeModels = new ArrayList<>();
            mxeModels.add(new MxeModel(modelId, modelVersion, weight1));
            mxeModels.add(new MxeModel(secondModelId, secondModelVersion, weight2));
            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME, mxeModels, initialInstanceNumber);
            serviceListInStep(mxeCliDriver);
            verifyServiceModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, mxeModels);
            verifyServiceInstanceNumber(mxeCliDriver, MXE_MODEL_SERVICE_NAME, initialInstanceNumber);
            waitUntilServiceStatusIs(mxeCliDriver, MXE_MODEL_SERVICE_NAME, STATUS_RUNNING, SERVICE_CREATE_TIMEOUT);
            serviceModifyABTestNonExistingModelExpectFail(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion);
            serviceModifyABTestNonExistingServiceExpectFail(mxeCliDriver, mxeModels);
            serviceModifyABTestWithOneModelExpectFail(mxeCliDriver, MXE_MODEL_SERVICE_NAME, modelId, modelVersion);
            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME);
            modelDelete(mxeCliDriver, modelId, modelVersion);
            modelDelete(mxeCliDriver, secondModelId, secondModelVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    private static void waitAfterServiceModify(MxeCliDriver mxeCliDriver) throws InterruptedException {
        TimeUnit.SECONDS.sleep(SLEEP_TIME_SEC_AFTER_MODIFY);
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
