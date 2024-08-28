package com.ericsson.mxe.jcat.test;

import com.ericsson.mxe.jcat.command.Commands;
import com.ericsson.mxe.jcat.command.CustomCommand;
import com.ericsson.mxe.jcat.command.MxeModelCommand;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.config.TestExecutionHost;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import com.ericsson.mxe.jcat.dto.MxeModel;
import com.google.common.collect.Lists;
import com.google.common.collect.MoreCollectors;
import org.apache.commons.lang3.StringUtils;
import se.ericsson.jcat.fluentassert.JcatFluentAssertApi;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.*;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.*;
import static se.ericsson.jcat.fluentassert.JcatFluentAssertApi.saveAssertThat;
import static se.ericsson.jcat.fluentassert.JcatFluentAssertApi.saveFail;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.*;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.*;

public class MxeModelTestHelper {

    public static final Duration MODEL_CREATE_TIMEOUT = Duration.ofMinutes(2);
    public static final Duration MODEL_START_TIMEOUT = Duration.ofMinutes(15);
    public static final Duration MODEL_ONBOARD_TIMEOUT = Duration.ofMinutes(15);

    private MxeModelTestHelper() {}

    static void deleteAllModelsInSubstep(TestExecutionHost testExecutionHost) {
        setSubTestStep("Delete models");
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            CustomCommand customCommand = Commands.mxeModel(testExecutionHost).listOnboarded();
            CommandResult commandResult = mxeCliDriver.execute(customCommand);
            assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
            List<Map<String, String>> result = MxeTestHelper.parseListCommandResult(commandResult);
            setTestInfoExpandable("Deployed models:", String.valueOf(result));
            result.forEach(map -> mxeCliDriver.execute(Commands.mxeModel(testExecutionHost)
                    .delete(map.get(MxeTestHelper.HEADER_ELEMENT_ID), map.get(HEADER_ELEMENT_VERSION))));
        } catch (Exception e) {
            setTestError(MxeTestHelper.ERROR_RESOURCE_RELEASE, e);
        }
        setSubTestStepEnd();
    }

    static Map<String, String> getModelFromList(List<Map<String, String>> modelList, String filterKey,
            String filterValue) {
        for (Map<String, String> model : modelList) {
            if (model.get(filterKey) != null && model.get(filterKey).equals(filterValue)) {
                return model;
            }
        }
        return null;
    }

    static List<Map<String, String>> getOnboardedModelList(final MxeCliDriver mxeCliDriver) {
        return getList(mxeCliDriver, Commands.mxeModel(mxeCliDriver.getTestExecutionHost()).listOnboarded());
    }

    static void modelDelete(final MxeCliDriver mxeCliDriver, String modelName, String modelVersion) {
        setTestStepBegin("Delete model " + modelName + " with version " + modelVersion);
        MxeModelCommand command =
                Commands.mxeModel(mxeCliDriver.getTestExecutionHost()).delete(modelName, modelVersion);
        String successPattern =
                "Success: Model \"" + modelName + "\" version \"" + modelVersion + "\" has been deleted from cluster.*";
        expectCommandToSucceed(mxeCliDriver, command, successPattern);
        setTestStepEnd();
    }

    static void modelDeleteFailed(final MxeCliDriver mxeCliDriver, String onboardedModelName,
            String onboardedModelVersion) {
        String notExistingModelName = UNKNOWN;
        String notExistingModelVersion = "0.0.9";

        setTestStepBegin("Delete model failure test");
        setSubTestStep("Delete started model");
        List<MxeModel> mxeModels = Lists.newArrayList(new MxeModel(onboardedModelName, onboardedModelVersion));
        String serviceName = "test-service";
        serviceCreate(mxeCliDriver, serviceName, mxeModels, 1);
        getServiceList(mxeCliDriver);
        CustomCommand customCommand = Commands.mxeModel(mxeCliDriver.getTestExecutionHost()).delete(onboardedModelName,
                onboardedModelVersion);
        CommandResult commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertNotEquals(MxeTestHelper.ERROR_EXIT_CODE_ZERO, 0, commandResult.getExitCode());
        saveAssertTrue(ERROR_OUTPUT_NOT_FOUND, commandResult.getCommandOutput().matches("Error: Model \""
                + onboardedModelName + "\" version \"" + onboardedModelVersion
                + "\" has an associated running model service. Please delete all the associated model services with the command mxe-service delete, and delete the model afterwards!"));
        serviceDelete(mxeCliDriver, serviceName);

        setSubTestStep("Delete non existing model");
        customCommand = Commands.mxeModel(mxeCliDriver.getTestExecutionHost()).delete(notExistingModelName,
                notExistingModelVersion);
        commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertNotEquals(MxeTestHelper.ERROR_EXIT_CODE_ZERO, 0, commandResult.getExitCode());
        saveAssertTrue(ERROR_OUTPUT_NOT_FOUND, commandResult.getCommandOutput().matches("Error: Model \""
                + notExistingModelName + "\" version \"" + notExistingModelVersion + "\" not found!"));

        setTestStepEnd();
    }

    static void modelDeleteNonExistingModelExpectFail(final MxeCliDriver mxeCliDriver) {
        setTestStepBegin("Delete non-existing model");
        String failName = "nonexistingname";
        String failVersion = "1.2.3";
        MxeModelCommand command = Commands.mxeModel(mxeCliDriver.getTestExecutionHost()).delete(failName, failVersion);
        String failPattern = "Error: Model \"" + failName + "\" version \"" + failVersion + "\" not found!";
        expectCommandToFail(mxeCliDriver, command, failPattern);
        setTestStepEnd();
    }

    static void modelListOnboardedInStep(final MxeCliDriver mxeCliDriver) {
        setTestStepBegin("List onboarded models");
        getOnboardedModelList(mxeCliDriver);
        setTestStepEnd();
    }

    static void modelOnboard(final MxeCliDriver mxeCliDriver, String modelName, String modelVersion,
            String packageName) {
        modelOnboard(mxeCliDriver, "Onboard model '" + modelName + "'", modelName, modelVersion, packageName);
    }

    static void modelOnboard(final MxeCliDriver mxeCliDriver, String stepName, String modelName, String modelVersion,
            String packageName) {
        setTestStepBegin(stepName);
        CustomCommand customCommand =
                Commands.mxeModel(mxeCliDriver.getTestExecutionHost()).onboard(modelName, modelVersion, packageName);
        CommandResult commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertEquals(ERROR_EXIT_CODE_NOT_ZERO, 0, commandResult.getExitCode());
        String successPattern = "Success: Model \"" + packageName + "\" has been onboarded to cluster \".*\" with ID \""
                + modelName + "\" and version \"" + modelVersion + "\".*";
        saveAssertThat(commandResult.getCommandOutput()).matches(successPattern).as(ERROR_OUTPUT_NOT_FOUND);
        setTestStepEnd();
    }

    static void modelOnboardAlreadyOnboardedExpectFail(final MxeCliDriver mxeCliDriver, String modelName,
            String modelVersion, String packageName) {
        modelOnboardExpectFail(mxeCliDriver, modelName, modelVersion, packageName,
                "Error: Model with image \"" + packageName + "\" has already been onboarded");
    }

    static void modelOnboardExpectFail(final MxeCliDriver mxeCliDriver, String modelName, String modelVersion,
            String packageName, String expectedErrorMessage) {
        modelOnboardExpectFail(mxeCliDriver, "Onboard model '" + modelName + "' expect fail", modelName, modelVersion,
                packageName, expectedErrorMessage);
    }

    static void modelOnboardExpectFail(final MxeCliDriver mxeCliDriver, String stepName, String modelName,
            String modelVersion, String packageName, String expectedErrorMessage) {
        setTestStepBegin(stepName);
        MxeModelCommand command =
                Commands.mxeModel(mxeCliDriver.getTestExecutionHost()).onboard(modelName, modelVersion, packageName);
        expectCommandToFail(mxeCliDriver, command, expectedErrorMessage);
        setTestStepEnd();
    }

    static void modelOnboardArchive(final MxeCliDriver mxeCliDriver, String modelId) {
        setSubTestStep("Onboard");
        File file = new File(modelId);
        setTestInfo("Archive model file: " + file.getAbsolutePath());
        if (!file.exists()) {
            JcatFluentAssertApi.fail(file.getAbsolutePath() + " does not exist");
        }
        try {
            CustomCommand customCommand =
                    Commands.mxeModel(mxeCliDriver.getTestExecutionHost()).onboardArchive(modelId);
            CommandResult commandResult = mxeCliDriver.executeSilent(customCommand);
            setTestInfo("<b>[result]</b><br/>" + MxeTestHelper.getLastLine(commandResult.getCommandOutput()));
            MxeTestHelper.assertResult(commandResult, "Success: Packaging has been started");
        } finally {
            setTestInfo("Deleting " + modelId);
            file.delete();
        }
        setSubTestStepEnd();
    }

    static void modelOnboardArchive(final MxeCliDriver mxeCliDriver, String modelId, String modelVersion,
            String packageName) {
        setTestStepBegin("Onboard model '" + modelId + "' from archive");
        modelOnboardArchive(mxeCliDriver, modelId + ".tar.gz");
        waitUntilInfoAppearsOnOnboardedModelList(mxeCliDriver, modelId, modelVersion, Duration.ofMinutes(2));
        waitUntilModelStatusIs(mxeCliDriver, modelId, modelVersion, STATUS_AVAILABLE, Duration.ofMinutes(30));
        setTestStepEnd();
    }

    static void modelOnboardArchiveExpectFail(final MxeCliDriver mxeCliDriver, String modelId) {
        setTestStepBegin("Onboard file '" + modelId + "' from archive which doesn't contain model");
        modelOnboardArchive(mxeCliDriver, modelId);
        waitUntilModelStatusIs(mxeCliDriver, getTemporalModelId(mxeCliDriver, modelId), UNKNOWN, STATUS_ERROR,
                Duration.ofMinutes(1));
        setTestStepEnd();
    }

    static void modelOnboardSource(final MxeCliDriver mxeCliDriver, String modelId, String sourcePath) {
        setTestStepBegin("Onboard model '" + modelId + "' from source");
        MxeModelCommand command = Commands.mxeModel(mxeCliDriver.getTestExecutionHost()).onboardSource(sourcePath);
        expectCommandToSucceed(mxeCliDriver, command, SUCCESS_PACKAGING_HAS_BEEN_STARTED);
        setTestStepEnd();
    }

    static void modelOnboardSourceExpectFail(final MxeCliDriver mxeCliDriver, String modelId, String sourcePath) {
        setTestStepBegin("Onboard faulty model '" + modelId + "' from source");
        MxeModelCommand command = Commands.mxeModel(mxeCliDriver.getTestExecutionHost()).onboardSource(sourcePath);
        expectCommandToFail(mxeCliDriver, command,
                String.format(MXE_META_INF_INFO_NO_SUCH_FILE_OR_DIRECTORY, sourcePath));
        setTestStepEnd();
    }

    static void checkModelNotFound(MxeCliDriver mxeCliDriver, String modelId, String modelVersion) {
        checkModelNotFound(mxeCliDriver, "Check model not found", modelId, modelVersion);
    }

    static void checkModelNotFound(MxeCliDriver mxeCliDriver, String stepName, String modelId, String modelVersion) {
        setTestStepBegin(stepName);
        Optional<Map<String, String>> model = getOnboardedModelList(mxeCliDriver).stream()
                .filter(m -> StringUtils.equals(m.get(HEADER_ELEMENT_ID), modelId))
                .filter(m -> StringUtils.equals(m.get(HEADER_ELEMENT_VERSION), modelVersion)).findFirst();
        saveAssertTrue("Model found which should not be exist", !model.isPresent() || model.get().isEmpty());
        setTestStepEnd();
    }

    static void waitUntilInfoAppearsOnOnboardedModelList(MxeCliDriver mxeCliDriver, String modelDeploymentId,
            String modelDeploymentVersion, Duration timeOut) {
        setTestStepBegin("Check if " + modelDeploymentId + ":" + modelDeploymentVersion + " info appeared '"
                + "' (timeout " + timeOut + ")");
        final long endTime = System.currentTimeMillis() + timeOut.toMillis();
        final int sleepSec = 10;

        int counter = 1;
        do {
            setSubTestStep("Check counter:" + counter++);
            MxeTestHelper.sleepSec(sleepSec);
            Optional<Map<String, String>> first = getOnboardedModelList(mxeCliDriver).stream()
                    .filter(m -> StringUtils.equals(m.get(MxeTestHelper.HEADER_ELEMENT_ID), modelDeploymentId))
                    .filter(m -> StringUtils.equals(m.get(HEADER_ELEMENT_VERSION), modelDeploymentVersion)).findFirst();
            if (first.isPresent()) {
                setTestInfo(modelDeploymentId + ":" + modelDeploymentVersion + " appeared");
                return;
            }

            setTestInfo("Waiting some more (%s left)", Duration.ofMillis(endTime - System.currentTimeMillis()));

        } while (System.currentTimeMillis() < endTime);
        saveFail("Info of " + modelDeploymentId + ":" + modelDeploymentVersion + " have not appeared '" + "' until "
                + timeOut);
    }

    static void waitUntilModelStatusIs(MxeCliDriver mxeCliDriver, String modelDeploymentId,
            String modelDeploymentVersion, String expectedStatus, Duration timeOut) {
        MxeModelCommand command = Commands.mxeModel(mxeCliDriver.getTestExecutionHost()).listOnboarded();
        waitUntilDeploymentStatusIs(mxeCliDriver, modelDeploymentId, modelDeploymentVersion, expectedStatus, timeOut,
                command);
    }

    @SuppressWarnings("UnstableApiUsage")
    static String getTemporalModelId(final MxeCliDriver mxeCliDriver, final String modelId) {
        return getOnboardedModelList(mxeCliDriver).stream()
                .filter(model -> StringUtils.contains(model.get(MxeTestHelper.HEADER_ELEMENT_ID), modelId))
                .filter(model -> StringUtils.equals(model.get(HEADER_ELEMENT_VERSION), UNKNOWN))
                .map(model -> model.get(MxeTestHelper.HEADER_ELEMENT_ID)).collect(MoreCollectors.onlyElement());
    }

}
