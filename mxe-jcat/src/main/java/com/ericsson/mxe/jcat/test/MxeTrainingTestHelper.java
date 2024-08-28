package com.ericsson.mxe.jcat.test;

import com.ericsson.mxe.jcat.command.Commands;
import com.ericsson.mxe.jcat.command.MxeTrainingCommand;
import com.ericsson.mxe.jcat.command.linux.LinuxCommand;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.config.TestExecutionHost;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import org.apache.commons.lang3.StringUtils;
import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.*;
import static se.ericsson.jcat.fluentassert.JcatFluentAssertApi.saveAssertThat;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertNotNull;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.saveAssertTrue;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.*;

public class MxeTrainingTestHelper {

    private MxeTrainingTestHelper() {}

    static void checkIfTrainingJobExists(MxeCliDriver mxeCliDriver, String trainingJobId) {
        setTestStepBegin("Check if training job '%s' exists", trainingJobId);

        MxeTrainingCommand listTrainingJobsCmd =
                Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).listTrainingJobs();
        Optional<CommandResult> resultOpt = executeCommand(mxeCliDriver, listTrainingJobsCmd);

        resultOpt.map(MxeTestHelper::parseListCommandResult).ifPresent(l -> saveAssertThat(l)
                .anyMatch(m -> m.getOrDefault(HEADER_ELEMENT_ID, StringUtils.EMPTY).equals(trainingJobId)));
        setTestStepEnd();
    }

    static void checkIfTrainingJobNotExists(MxeCliDriver mxeCliDriver, String trainingJobId) {
        setTestStepBegin("Check if training job '%s' NOT exists", trainingJobId);

        MxeTrainingCommand listTrainingJobsCmd =
                Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).listTrainingJobs();
        Optional<CommandResult> resultOpt = executeCommand(mxeCliDriver, listTrainingJobsCmd);

        resultOpt.map(MxeTestHelper::parseListCommandResult).ifPresent(l -> saveAssertThat(l)
                .noneMatch(m -> m.getOrDefault(HEADER_ELEMENT_ID, StringUtils.EMPTY).equals(trainingJobId)));
        setTestStepEnd();
    }

    static void checkIfTrainingPackageExists(MxeCliDriver mxeCliDriver, String trainingPackageName) {
        setTestStepBegin("Check if training package '%s' exists", trainingPackageName);

        MxeTrainingCommand listTrainingPackagesCmd =
                Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).listTrainingPackages();
        Optional<CommandResult> resultOpt = executeCommand(mxeCliDriver, listTrainingPackagesCmd);

        resultOpt.map(r -> String.valueOf(r.getCommandOutput()))
                .ifPresent(r -> saveAssertThat(r.split("\n")).contains(trainingPackageName));
        setTestStepEnd();
    }

    static void checkIfTrainingPackageNotExists(MxeCliDriver mxeCliDriver, String trainingPackageName) {
        setTestStepBegin("Check if training package '%s' NOT exists", trainingPackageName);

        MxeTrainingCommand listTrainingPackagesCmd =
                Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).listTrainingPackages();
        Optional<CommandResult> resultOpt = executeCommand(mxeCliDriver, listTrainingPackagesCmd);

        resultOpt.map(r -> String.valueOf(r.getCommandOutput()))
                .ifPresent(r -> saveAssertThat(r.split("\n")).doesNotContain(trainingPackageName));
        setTestStepEnd();
    }

    static void deleteAllTrainingPackagesInSubstep(TestExecutionHost testExecutionHost) {
        setSubTestStep("Delete training packages");
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            MxeTrainingCommand customCommand = Commands.mxeTraining(testExecutionHost).listTrainingPackages();
            CommandResult commandResult = mxeCliDriver.execute(customCommand);
            assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
            List<Map<String, String>> result = MxeTestHelper.parseListCommandResult(commandResult);
            setTestInfoExpandable("Deployed training packages:", String.valueOf(result));
            result.forEach(map -> mxeCliDriver.execute(Commands.mxeTraining(testExecutionHost)
                    .deleteTrainingPackage(map.get(MxeTestHelper.HEADER_ELEMENT_ID), map.get(HEADER_ELEMENT_VERSION))));
        } catch (Exception e) {
            setTestError(MxeTestHelper.ERROR_RESOURCE_RELEASE, e);
        }
        setSubTestStepEnd();
    }

    static void deleteAllTraininigJobsInSubstep(TestExecutionHost testExecutionHost) {
        setSubTestStep("Delete training jobs");
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            MxeTrainingCommand command = Commands.mxeTraining(testExecutionHost).listTrainingJobs();
            CommandResult commandResult = mxeCliDriver.execute(command);
            assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
            List<Map<String, String>> result = MxeTestHelper.parseListCommandResult(commandResult);
            setTestInfoExpandable("Deployed training jobs:", String.valueOf(result));
            result.forEach(
                    map -> mxeCliDriver.execute(Commands.mxeTraining(testExecutionHost).deleteTrainingJobByPackage(
                            map.get(MxeTestHelper.HEADER_ELEMENT_ID), map.get(HEADER_ELEMENT_VERSION))));
        } catch (Exception e) {
            setTestError(MxeTestHelper.ERROR_RESOURCE_RELEASE, e);
        }
        setSubTestStepEnd();
    }

    static List<Map<String, String>> getOnboardedTrainingPackageList(final MxeCliDriver mxeCliDriver) {
        return getList(mxeCliDriver, Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).listTrainingPackages());
    }

    static List<Map<String, String>> getStartedTrainingJobList(final MxeCliDriver mxeCliDriver) {
        return getList(mxeCliDriver, Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).listTrainingJobs());
    }

    static void trainingJobDeleteById(final MxeCliDriver mxeCliDriver, String id) {
        setTestStepBegin("Delete training job " + id);
        MxeTrainingCommand command =
                Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).deleteTrainingJobById(id);
        String successPattern = "Success: Training job with id \"" + id + "\" has been removed from cluster.*";
        expectCommandToSucceed(mxeCliDriver, command, successPattern);
        setTestStepEnd();
    }

    static void trainingJobDeleteByPackage(final MxeCliDriver mxeCliDriver, String packageId, String packageVersion) {
        setTestStepBegin("Delete training job " + packageId + " with version " + packageVersion);
        MxeTrainingCommand command = Commands.mxeTraining(mxeCliDriver.getTestExecutionHost())
                .deleteTrainingJobByPackage(packageId, packageVersion);
        String successPattern = "Success: Training jobs with packageId \"" + packageId + "\" and packageVersion \""
                + packageVersion + "\" have been removed from cluster.*";
        expectCommandToSucceed(mxeCliDriver, command, successPattern);
        setTestStepEnd();
    }

    static void trainingJobDeleteNonExistingPackageIdVersionExpectFail(final MxeCliDriver mxeCliDriver) {
        setTestStepBegin("Delete non-existing training jobs");
        String failName = "nonexistingname";
        String failVersion = "1.2.3";
        MxeTrainingCommand command = Commands.mxeTraining(mxeCliDriver.getTestExecutionHost())
                .deleteTrainingJobByPackage(failName, failVersion);
        String failPattern = "Error: No training job found with packageId \"" + failName + "\" and packageVersion \""
                + failVersion + "\"";
        expectCommandToFail(mxeCliDriver, command, failPattern);
        setTestStepEnd();
    }

    static void trainingJobDeleteNonExistingJobIdExpectFail(final MxeCliDriver mxeCliDriver) {
        setTestStepBegin("Delete non-existing training jobs");
        String id = "nonexistingname";
        MxeTrainingCommand command =
                Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).deleteTrainingJobById(id);
        String failPattern = "Error: Training job with id \"" + id + "\" not found!";
        expectCommandToFail(mxeCliDriver, command, failPattern);
        setTestStepEnd();
    }

    static void trainingJobListInStep(final MxeCliDriver mxeCliDriver) {
        setTestStepBegin("List started training jobs");
        getStartedTrainingJobList(mxeCliDriver);
        setTestStepEnd();
    }

    static String trainingJobStart(final MxeCliDriver mxeCliDriver, String packageId, String packageVersion) {
        final MxeTrainingCommand command =
                Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).start(packageId, packageVersion);
        String successPattern = "Training package started with Id \"([a-zA-Z-0-9]+)\"";
        return startDeployment(mxeCliDriver, command, successPattern);
    }

    static String trainingJobStartInStep(final MxeCliDriver mxeCliDriver, String packageId, String packageVersion) {
        setTestStepBegin("Start training job " + packageId + " with version " + packageVersion);
        String returnedInstanceName = trainingJobStart(mxeCliDriver, packageId, packageVersion);
        setTestStepEnd();
        return returnedInstanceName;
    }

    static void trainingPackageDelete(final MxeCliDriver mxeCliDriver, String packageId, String packageVersion) {
        setTestStepBegin("Delete training package " + packageId + " with version " + packageVersion);
        MxeTrainingCommand command = Commands.mxeTraining(mxeCliDriver.getTestExecutionHost())
                .deleteTrainingPackage(packageId, packageVersion);
        String successPattern = "Success: Training package \"" + packageId + "\" version \"" + packageVersion
                + "\" has been deleted from cluster.*";
        expectCommandToSucceed(mxeCliDriver, command, successPattern);
        setTestStepEnd();
    }

    static void trainingPackageDeleteNonExistingExpectFail(final MxeCliDriver mxeCliDriver) {
        setTestStepBegin("Delete non-existing training package");
        String failName = "nonexistingname";
        String failVersion = "1.2.3";
        MxeTrainingCommand command =
                Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).deleteTrainingPackage(failName, failVersion);
        String failPattern = "Error: Training package \"" + failName + "\" version \"" + failVersion + "\" not found!";
        expectCommandToFail(mxeCliDriver, command, failPattern);
        setTestStepEnd();
    }

    static void trainingPackageInPackagingDeleteExpectFail(final MxeCliDriver mxeCliDriver, String packageId,
            String packageVersion) {
        setTestStepBegin("Delete training package '" + packageId + "', for package in packaging state");
        MxeTrainingCommand command = Commands.mxeTraining(mxeCliDriver.getTestExecutionHost())
                .deleteTrainingPackage(packageId, packageVersion);
        String failPattern = "Error: Training package \"" + packageId + "\" version \"" + packageVersion
                + "\" is being onboarded, it can not be deleted at the moment. Please try it later!";
        expectCommandToFail(mxeCliDriver, command, failPattern);
        setTestStepEnd();
    }

    static void trainingPackageListInStep(final MxeCliDriver mxeCliDriver) {
        setTestStepBegin("List onboarded training packages");
        getOnboardedTrainingPackageList(mxeCliDriver);
        setTestStepEnd();
    }

    static void trainingPackageOnboardAlreadyOnboardedExpectFail(final MxeCliDriver mxeCliDriver, String packageId,
            String packageVersion, String sourcePath) {
        setTestStepBegin("Onboard training package '" + packageId + "', for a source which has been already onboarded");
        MxeTrainingCommand onboardCommand =
                Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).onboard(sourcePath);
        executeCommand(mxeCliDriver, onboardCommand);
        sleepSec(30);
        List<Map<String, String>> trainingPackages = getOnboardedTrainingPackageList(mxeCliDriver);
        String failPattern =
                "ERROR: A model already exists with id \"" + packageId + "\" and version \"" + packageVersion + "\"";
        String sourceZip = sourcePath.substring(sourcePath.lastIndexOf("/") + 1) + ".zip";
        saveAssertTrue("Could not find training package with \"" + failPattern + "\" status",
                trainingPackages.stream().filter(p -> p.get(HEADER_ELEMENT_ID).startsWith(sourceZip)
                        && p.get(HEADER_ELEMENT_STATUS).equals(failPattern)).findFirst().isPresent());
        setTestStepEnd();
    }

    static void trainingPackageOnboardSource(final MxeCliDriver mxeCliDriver, String packageId, String sourcePath) {
        setTestStepBegin("Onboard training package '" + packageId + "' from source");
        MxeTrainingCommand command = Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).onboard(sourcePath);
        expectCommandToSucceed(mxeCliDriver, command, SUCCESS_PACKAGING_HAS_BEEN_STARTED);
        setTestStepEnd();
    }

    static void trainingPackageOnboardSourceExpectFail(final MxeCliDriver mxeCliDriver, String packageId,
            String sourcePath) {
        setTestStepBegin("Onboard faulty training package '" + packageId + "' from source");
        MxeTrainingCommand command = Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).onboard(sourcePath);
        expectCommandToFail(mxeCliDriver, command,
                String.format(MXE_META_INF_INFO_NO_SUCH_FILE_OR_DIRECTORY, sourcePath));
        setTestStepEnd();
    }

    static void trainingPackageWithRunningJobDeleteExpectFail(final MxeCliDriver mxeCliDriver, String packageId,
            String packageVersion) {
        setTestStepBegin(
                "Delete training package '" + packageId + "', for package with training job(s) in running state");
        MxeTrainingCommand command = Commands.mxeTraining(mxeCliDriver.getTestExecutionHost())
                .deleteTrainingPackage(packageId, packageVersion);
        String failPattern = "Error: Training package \"" + packageId + "\" version \"" + packageVersion
                + "\" can not be deleted, training job is still running. Please try it later or delete running job directly!\\nrunning job id:.*";
        expectCommandToFail(mxeCliDriver, command, failPattern);
        setTestStepEnd();
    }

    static void waitUntilTrainingJobStatusIs(MxeCliDriver mxeCliDriver, String instanceName, String expectedStatus,
            Duration timeOut) {
        MxeTrainingCommand command = Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).listTrainingJobs();
        waitUntilRunningDeploymentStatusIs(mxeCliDriver, instanceName, expectedStatus, timeOut,
                MxeTestHelper.HEADER_ELEMENT_ID, command);
    }

    static void waitUntilTrainingPackageStatusIs(MxeCliDriver mxeCliDriver, String trainingPackageId,
            String trainingPackageVersion, String expectedStatus, Duration timeOut) {
        MxeTrainingCommand command = Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).listTrainingPackages();
        waitUntilDeploymentStatusIs(mxeCliDriver, trainingPackageId, trainingPackageVersion, expectedStatus, timeOut,
                command);
    }

    static void downloadTrainingResult(MxeCliDriver mxeCliDriver, String jobId) {
        setTestStepBegin("Download existing training result '%s'", jobId);
        MxeTrainingCommand command =
                Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).downloadTrainingResult(jobId);
        expectCommandToSucceed(mxeCliDriver, command, ".*Success: Download finished");

        String pwd = mxeCliDriver.execute(new LinuxCommand("pwd")).getCommandOutput().trim();
        File result = Paths.get(pwd, String.format("training-job-result-%s.zip", jobId)).toFile();
        saveAssertTrue(String.format("Downloaded file (%s) is not found.", result.getAbsolutePath()), result.exists());
        saveAssertTrue(String.format("Downloaded file (%s) is empty.", result.getAbsolutePath()), result.length() > 0);

        setTestStepEnd();
    }

    static void downloadTrainingResultWithTargetDirectory(MxeCliDriver mxeCliDriver, String jobId, String toDir) {
        setTestStepBegin("Download existing training result '%s' to '%s'", jobId, toDir);
        MxeTrainingCommand command =
                Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).downloadTrainingResult(jobId, toDir);
        expectCommandToSucceed(mxeCliDriver, command, ".*Success: Download finished");

        String pwd = mxeCliDriver.execute(new LinuxCommand("pwd")).getCommandOutput().trim();
        File result = Paths.get(pwd, toDir, String.format("training-job-result-%s.zip", jobId)).toFile();
        saveAssertTrue(String.format("Downloaded file (%s) is not found.", result.getAbsolutePath()), result.exists());
        saveAssertTrue(String.format("Downloaded file (%s) is empty.", result.getAbsolutePath()), result.length() > 0);

        setTestStepEnd();
    }

    static void downloadNotExistingTrainingResult(MxeCliDriver mxeCliDriver, String jobId) {
        setTestStepBegin("Download not existing training result '%s'", jobId);
        MxeTrainingCommand command =
                Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).downloadTrainingResult(jobId);
        expectCommandToFail(mxeCliDriver, command, ".*Error: Training job with id \"" + jobId + "\" not found");
        setTestStepEnd();
    }

    static void downloadNotFinishedTrainingResult(MxeCliDriver mxeCliDriver, String jobId) {
        setTestStepBegin("Download not finished training result '%s'", jobId);
        MxeTrainingCommand command =
                Commands.mxeTraining(mxeCliDriver.getTestExecutionHost()).downloadTrainingResult(jobId);
        expectCommandToFail(mxeCliDriver, command, ".*Error: Training job is still running");
        setTestStepEnd();
    }
}
