package com.ericsson.mxe.jcat.test;

import com.ericsson.mxe.jcat.command.Commands;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.JcatClass;
import se.ericsson.jcat.fw.annotations.JcatMethod;
import se.ericsson.jcat.fw.annotations.Teardown;
import se.ericsson.jcat.fw.model.JcatModelHolder;
import java.time.Duration;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.*;
import static com.ericsson.mxe.jcat.test.MxeTrainingTestHelper.*;

/**
 * @JcatDocChapterDescription Chapter covering model function tests.
 */
@JcatClass(chapterName = "Model Function Tests")
public class MxeTrainingTest extends MxeTestBase {

    private static final Duration TRAINING_START_TIMEOUT = Duration.ofMinutes(15);
    private static final Duration TRAINING_ONBOARD_TIMEOUT = Duration.ofMinutes(20);
    private static final String STATUS_RUNNING = "running";

    /**
     * @JcatTcDescription List MXE training commands
     * @JcatTcPreconditions None
     * @JcatTcInstruction The testcase is about running the mxe-training CLI to print list related help messages using
     *                    default cluster
     * @JcatTcAction mxe-training list --help --verbose
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction mxe-training list --verbose
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction mxe-training list --help
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction mxe-training list
     * @JcatTcActionResult Help message is printed
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-TRAINING-COMMANDS", testTitle = "List MXE training commands")
    public void testMxeTrainingPrintHelp() {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            executeInStep("Training list help verbose", mxeCliDriver,
                    () -> Commands.mxeTraining(testExecutionHost).list().help().verbose());
            executeInStep("Training list verbose", mxeCliDriver,
                    () -> Commands.mxeTraining(testExecutionHost).list().verbose());
            executeInStep("Training list help", mxeCliDriver,
                    () -> Commands.mxeTraining(testExecutionHost).list().help());
            executeInStep("Training list", mxeCliDriver, () -> Commands.mxeTraining(testExecutionHost).list());
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Onboard a training package from source on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about onboarding an mxe training package
     * @JcatTcAction Onboard a faulty training package from source
     * @JcatTcActionResult Training package onboard failed, because there is no INFO file in it
     * @JcatTcAction Onboard the training package from source
     * @JcatTcActionResult Training package is onboarded successfully
     * @JcatTcAction List onboarded training packages and wait for available status
     * @JcatTcActionResult Previously onboarded training package appears in the training package list as available
     * @JcatTcAction Start the onboarded training package
     * @JcatTcActionResult Training job started successfully
     * @JcatTcAction List started training jobs and wait for completed status
     * @JcatTcActionResult Previously started training job appears in the training job list as completed
     * @JcatTcAction Delete training job by the training package id and version
     * @JcatTcActionResult Training job deleted successfully
     * @JcatTcAction Start the onboarded training package
     * @JcatTcActionResult Training job started successfully
     * @JcatTcAction List started training jobs and wait for completed status
     * @JcatTcActionResult Previously started training job appears in the training job list as completed
     * @JcatTcAction Delete training job by id
     * @JcatTcActionResult Training job deleted successfully
     * @JcatTcAction Delete training package by package id and version
     * @JcatTcActionResult Training package deleted successfully
     * @JcatTcAction Start the onboarded training package
     * @JcatTcActionResult Training job started successfully
     * @JcatTcAction Delete training package by package id and version
     * @JcatTcActionResult Training package and connecting jobs are deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"trainingPackageSourcePath", "sourceTrainingPackageName", "trainingPackageVersion"})
    @JcatMethod(testTag = "TEST-MXE-TRAININGPACKAGE-ONBOARDSOURCE",
            testTitle = "Training package onboarding from source")
    public void testMxeTrainingPackageOnboardFromSource(String trainingPackageSourcePath,
            String sourceTrainingPackageName, String trainingPackageVersion) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            trainingPackageOnboardSource(mxeCliDriver, sourceTrainingPackageName, trainingPackageSourcePath);
            waitUntilTrainingPackageStatusIs(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion,
                    STATUS_AVAILABLE, TRAINING_ONBOARD_TIMEOUT);

            String instanceName =
                    trainingJobStartInStep(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion);
            waitUntilTrainingJobStatusIs(mxeCliDriver, instanceName, STATUS_COMPLETED, TRAINING_START_TIMEOUT);
            trainingJobDeleteByPackage(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion);
            checkIfTrainingJobNotExists(mxeCliDriver, instanceName);

            instanceName = trainingJobStartInStep(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion);
            waitUntilTrainingJobStatusIs(mxeCliDriver, instanceName, STATUS_COMPLETED, TRAINING_START_TIMEOUT);
            trainingJobDeleteById(mxeCliDriver, instanceName);
            checkIfTrainingJobNotExists(mxeCliDriver, instanceName);
            instanceName = trainingJobStartInStep(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion);
            waitUntilTrainingJobStatusIs(mxeCliDriver, instanceName, STATUS_COMPLETED, TRAINING_START_TIMEOUT);
            trainingPackageDelete(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion);
            checkIfTrainingJobNotExists(mxeCliDriver, instanceName);
            checkIfTrainingPackageNotExists(mxeCliDriver, sourceTrainingPackageName);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Onboard a training package, start a training job and delete it after on local cluster:
     *                    negative cases
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about testing the negative cases of onboard, start and delete an mxe training
     *                    package, job
     * @JcatTcAction Onboard the training package
     * @JcatTcActionResult Training package is onboarded successfully
     * @JcatTcAction Onboard the training package again from the same source
     * @JcatTcActionResult Training package onboard should fail
     * @JcatTcAction List onboarded training packages
     * @JcatTcActionResult Previously onboarded training package appears in the training package list
     * @JcatTcAction Start the onboarded training package
     * @JcatTcActionResult Training job started successfully
     * @JcatTcAction List started training jobs
     * @JcatTcActionResult Previously started training job appears in the training job list
     * @JcatTcAction Start the already started training job again
     * @JcatTcActionResult Training job start should fail
     * @JcatTcAction Start non-existing training package
     * @JcatTcActionResult Training job start should fail
     * @JcatTcAction Delete training job
     * @JcatTcActionResult Training job deleted successfully
     * @JcatTcAction Delete a non-existing training job
     * @JcatTcActionResult Training job delete should fail
     * @JcatTcAction Delete training package
     * @JcatTcActionResult Training package deleted successfully
     * @JcatTcAction Delete non-existing training package
     * @JcatTcActionResult Training package delete should fail
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"trainingPackageSourcePath", "faultyTrainingPackageSourcePath", "sourceTrainingPackageName",
            "trainingPackageVersion"})
    @JcatMethod(testTag = "TEST-MXE-TRAINING-NEGATIVE-CASES",
            testTitle = "Onboard a training package, start a training job and delete them on local cluster: negative cases")
    public void testMxeTrainingJobNegativeCases(String trainingPackageSourcePath,
            String faultyTrainingPackageSourcePath, String sourceTrainingPackageName, String trainingPackageVersion) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            trainingPackageOnboardSourceExpectFail(mxeCliDriver, sourceTrainingPackageName,
                    faultyTrainingPackageSourcePath);
            trainingPackageOnboardSource(mxeCliDriver, sourceTrainingPackageName, trainingPackageSourcePath);
            waitUntilTrainingPackageStatusIs(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion,
                    STATUS_PACKAGING, TRAINING_ONBOARD_TIMEOUT);
            trainingPackageInPackagingDeleteExpectFail(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion);
            waitUntilTrainingPackageStatusIs(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion,
                    STATUS_AVAILABLE, TRAINING_ONBOARD_TIMEOUT);
            trainingPackageOnboardAlreadyOnboardedExpectFail(mxeCliDriver, sourceTrainingPackageName,
                    trainingPackageVersion, trainingPackageSourcePath);
            trainingPackageListInStep(mxeCliDriver);

            String instanceName =
                    trainingJobStartInStep(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion);
            waitUntilTrainingJobStatusIs(mxeCliDriver, instanceName, STATUS_RUNNING, TRAINING_START_TIMEOUT);
            trainingJobListInStep(mxeCliDriver);
            trainingPackageWithRunningJobDeleteExpectFail(mxeCliDriver, sourceTrainingPackageName,
                    trainingPackageVersion);
            trainingJobDeleteById(mxeCliDriver, instanceName);
            checkIfTrainingJobNotExists(mxeCliDriver, instanceName);
            trainingJobDeleteNonExistingPackageIdVersionExpectFail(mxeCliDriver);
            trainingJobDeleteNonExistingJobIdExpectFail(mxeCliDriver);
            trainingPackageDelete(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion);
            checkIfTrainingPackageNotExists(mxeCliDriver, sourceTrainingPackageName);
            trainingPackageDeleteNonExistingExpectFail(mxeCliDriver);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Download a training result from local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about downloading an mxe training result
     * @JcatTcAction Onboard the training package from source
     * @JcatTcActionResult Training package is onboarded successfully
     * @JcatTcAction List onboarded training packages and wait for available status
     * @JcatTcActionResult Previously onboarded training package appears in the training package list as available
     * @JcatTcAction Start the onboarded training package
     * @JcatTcActionResult Training job started successfully
     * @JcatTcAction List started training jobs and wait for completed status
     * @JcatTcActionResult Previously started training job appears in the training job list as completed
     * @JcatTcAction Download training result by id
     * @JcatTcActionResult Training result downloaded successfully
     * @JcatTcAction Download training result by id into target directory
     * @JcatTcActionResult Training result downloaded successfully to the target dir
     * @JcatTcAction Delete training job by id
     * @JcatTcActionResult Training job deleted successfully
     * @JcatTcAction Delete training package by package id and version
     * @JcatTcActionResult Training package deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"trainingPackageSourcePath", "sourceTrainingPackageName", "trainingPackageVersion"})
    @JcatMethod(testTag = "TEST-MXE-TRAININGJOB-RESULT-DOWNLOAD",
            testTitle = "Download a training result from local cluster")
    public void testMxeTrainingJobResultDownload(String trainingPackageSourcePath, String sourceTrainingPackageName,
            String trainingPackageVersion) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            trainingPackageOnboardSource(mxeCliDriver, sourceTrainingPackageName, trainingPackageSourcePath);
            waitUntilTrainingPackageStatusIs(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion,
                    STATUS_AVAILABLE, TRAINING_ONBOARD_TIMEOUT);

            String instanceName =
                    trainingJobStartInStep(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion);
            waitUntilTrainingJobStatusIs(mxeCliDriver, instanceName, STATUS_COMPLETED, TRAINING_START_TIMEOUT);

            downloadTrainingResult(mxeCliDriver, instanceName);

            downloadTrainingResultWithTargetDirectory(mxeCliDriver, instanceName, "testDir");

            trainingJobDeleteById(mxeCliDriver, instanceName);
            checkIfTrainingJobNotExists(mxeCliDriver, instanceName);
            trainingPackageDelete(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Download a training result from local cluster: negative cases
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about downloading an mxe training result
     * @JcatTcAction Onboard the training package from source
     * @JcatTcActionResult Training package is onboarded successfully
     * @JcatTcAction List onboarded training packages and wait for available status
     * @JcatTcActionResult Previously onboarded training package appears in the training package list as available
     * @JcatTcAction Start the onboarded training package
     * @JcatTcActionResult Training job started successfully
     * @JcatTcAction Download training result by id
     * @JcatTcActionResult Training result didn't download, because job not finished yet
     * @JcatTcAction List started training jobs and wait for completed status
     * @JcatTcActionResult Previously started training job appears in the training job list as completed
     * @JcatTcAction Delete training job by id
     * @JcatTcActionResult Training job deleted successfully
     * @JcatTcAction Download training result by id
     * @JcatTcActionResult Training result didn't download, because result not exist
     * @JcatTcAction Delete training package by package id and version
     * @JcatTcActionResult Training package deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({"trainingPackageSourcePath", "sourceTrainingPackageName", "trainingPackageVersion"})
    @JcatMethod(testTag = "TEST-MXE-TRAININGJOB-RESULT-DOWNLOAD-NEGATIVE",
            testTitle = " Download a training result from local cluster: negative cases")
    public void testMxeTrainingJobResultDownloadNegativeCases(String trainingPackageSourcePath,
            String sourceTrainingPackageName, String trainingPackageVersion) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            trainingPackageOnboardSource(mxeCliDriver, sourceTrainingPackageName, trainingPackageSourcePath);
            waitUntilTrainingPackageStatusIs(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion,
                    STATUS_AVAILABLE, TRAINING_ONBOARD_TIMEOUT);

            String instanceName =
                    trainingJobStartInStep(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion);

            downloadNotFinishedTrainingResult(mxeCliDriver, instanceName);

            waitUntilTrainingJobStatusIs(mxeCliDriver, instanceName, STATUS_COMPLETED, TRAINING_START_TIMEOUT);

            trainingJobDeleteById(mxeCliDriver, instanceName);
            checkIfTrainingJobNotExists(mxeCliDriver, instanceName);

            downloadNotExistingTrainingResult(mxeCliDriver, instanceName);

            trainingPackageDelete(mxeCliDriver, sourceTrainingPackageName, trainingPackageVersion);
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
                deleteAllTraininigJobsInSubstep(testExecutionHost);
                deleteAllTrainingPackagesInSubstep(testExecutionHost);
                setTestStepEnd();
            }
            default: {
                // NOOP
            }
        }
    }
}
