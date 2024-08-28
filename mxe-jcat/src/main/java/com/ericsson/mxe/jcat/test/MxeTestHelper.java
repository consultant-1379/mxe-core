package com.ericsson.mxe.jcat.test;

import com.ericsson.mxe.jcat.command.CustomCommand;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.ericsson.jcat.fluentassert.JcatFluentAssertApi;
import se.ericsson.jcat.fw.assertion.JcatAssertApi;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.hamcrest.Matchers.is;
import static se.ericsson.jcat.fluentassert.JcatFluentAssertApi.assertThat;
import static se.ericsson.jcat.fluentassert.JcatFluentAssertApi.saveAssertThat;
import static se.ericsson.jcat.fluentassert.JcatFluentAssertApi.saveFail;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.saveAssertThat;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.*;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.*;

public class MxeTestHelper {

    static final String ERROR_EXIT_CODE_NOT_ZERO = "CustomCommand exit code was not 0";
    static final String ERROR_EXIT_CODE_ZERO = "Command exit code was 0";
    static final String ERROR_FAILED_TO_EXECUTE = "Failed to execute command, commandResult is null";
    static final String ERROR_OUTPUT_NOT_FOUND = "Expected output not found";
    static final String ERROR_RESOURCE_RELEASE = "Could not release cli driver resources";
    static final String HEADER_ELEMENT_ENDPOINT = "ENDPOINT";
    static final String HEADER_ELEMENT_ID = "ID";
    static final String HEADER_ELEMENT_INSTANCES = "INSTANCES";
    static final String HEADER_ELEMENT_WEIGHTS = "WEIGHTS";
    static final String HEADER_ELEMENT_MODEL = "MODEL";
    static final String HEADER_ELEMENT_MODEL_A = "MODEL_A";
    static final String HEADER_ELEMENT_MODEL_B = "MODEL_B";
    static final String HEADER_ELEMENT_NAME = "NAME";
    static final String HEADER_ELEMENT_DOMAIN = "DOMAIN";
    static final String HEADER_ELEMENT_STATUS = "STATUS";
    static final String HEADER_ELEMENT_VERSION = "VERSION";
    static final String HEADER_ELEMENT_AUTOSCALING = "AUTOSCALING";
    static final String MXE_META_INF_INFO_NO_SUCH_FILE_OR_DIRECTORY =
            ".*Error: open %s/MXE-META-INF/INFO: no such file or directory";
    static final String STATUS_AVAILABLE = "available";
    static final String STATUS_COMPLETED = "completed";
    static final String STATUS_CREATING = "creating";
    static final String STATUS_ERROR = "ERROR";
    static final String STATUS_PACKAGING = "packaging";
    static final String STATUS_RUNNING = "running";
    static final String SUCCESS_PACKAGING_HAS_BEEN_STARTED = ".*Success: Packaging has been started";
    static final String UNKNOWN = "unknown";
    static final String MODEL_VERSION = "1.2.3";

    private static final Logger LOGGER = LoggerFactory.getLogger(MxeTestHelper.class);

    private MxeTestHelper() {}

    static void assertResult(CommandResult commandResult, Set<String> successPatterns) {
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertEquals(ERROR_EXIT_CODE_NOT_ZERO, 0, commandResult.getExitCode());
        boolean match = false;

        for (String successPattern : successPatterns) {
            Matcher matcher = Pattern.compile(Pattern.quote(successPattern)).matcher(commandResult.getCommandOutput());
            if (matcher.find()) {
                match = true;
                break;
            }
        }
        if (!match) {
            StringBuilder sb = new StringBuilder();
            sb.append(". Expected one of the following ones:\n");
            successPatterns.forEach(p -> sb.append(p).append('\n'));
            sb.append("but found: ").append(commandResult.getCommandOutput());
            JcatAssertApi.saveFail(ERROR_OUTPUT_NOT_FOUND + sb.toString());
        }
    }

    static void assertResult(CommandResult commandResult, String successPattern) {
        MxeTestHelper.assertResult(commandResult, Sets.newHashSet(successPattern));
    }

    static Optional<CommandResult> executeCommand(final MxeCliDriver mxeCliDriver, final CustomCommand customCommand) {
        final CommandResult commandResult = mxeCliDriver.execute(customCommand);
        if (Objects.nonNull(commandResult)) {
            final int exitCode = commandResult.getExitCode();
            saveAssertThat(ERROR_EXIT_CODE_NOT_ZERO, exitCode, is(0));
        } else {
            JcatAssertApi.saveFail("Command was not executed");
        }
        return Optional.ofNullable(commandResult);
    }

    static void executeCommandInStep(final MxeCliDriver mxeCliDriver, final CustomCommand customCommand) {
        MxeTestHelper.executeCommandInStep(mxeCliDriver, customCommand, customCommand.getSyntax());
    }

    static void executeCommandInStep(final MxeCliDriver mxeCliDriver, final CustomCommand customCommand,
            final String stepName) {
        setTestStepBegin(stepName);
        executeCommand(mxeCliDriver, customCommand);
        setTestStepEnd();
    }

    static void executeInStep(final String stepName, final MxeCliDriver mxeCliDriver,
            final Supplier<CustomCommand> customCommandSupplier) {
        setTestStepBegin(stepName);
        CustomCommand customCommand = customCommandSupplier.get();
        executeCommand(mxeCliDriver, customCommand);
        setTestStepEnd();
    }

    static void expectCommandToFail(final MxeCliDriver mxeCliDriver, CustomCommand command, String messagePattern) {
        CommandResult commandResult = mxeCliDriver.execute(command);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertNotEquals(MxeTestHelper.ERROR_EXIT_CODE_ZERO, 0, commandResult.getExitCode());
        saveAssertThat(commandResult.getCommandOutput()).matches(Pattern.compile(messagePattern, Pattern.DOTALL))
                .as(ERROR_OUTPUT_NOT_FOUND);
    }

    static void expectCommandToSucceedWithRetries(final MxeCliDriver mxeCliDriver, CustomCommand command,
            String messagePattern, List<Integer> sleepSecBetweenRetries) {
        for (int i = 0; i <= sleepSecBetweenRetries.size(); i++) {
            CommandResult commandResult = mxeCliDriver.execute(command);
            assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
            if (i == sleepSecBetweenRetries.size()) {
                saveAssertEquals(MxeTestHelper.ERROR_EXIT_CODE_NOT_ZERO, 0, commandResult.getExitCode());
                saveAssertThat(commandResult.getCommandOutput())
                        .matches(Pattern.compile(messagePattern, Pattern.DOTALL)).as(ERROR_OUTPUT_NOT_FOUND);
                break;
            }
            if (commandResult.getExitCode() != 0) {
                LOGGER.warn(ERROR_EXIT_CODE_NOT_ZERO);
                sleepSec(sleepSecBetweenRetries.get(i));
                continue;
            }
            final Matcher matcher =
                    Pattern.compile(messagePattern, Pattern.DOTALL).matcher(commandResult.getCommandOutput());
            if (!matcher.matches()) {
                LOGGER.warn(ERROR_OUTPUT_NOT_FOUND);
                sleepSec(sleepSecBetweenRetries.get(i));
                continue;
            }
            break;
        }
    }

    static void expectCommandToSucceed(final MxeCliDriver mxeCliDriver, CustomCommand command, String messagePattern) {
        CommandResult commandResult = mxeCliDriver.execute(command);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertEquals(MxeTestHelper.ERROR_EXIT_CODE_NOT_ZERO, 0, commandResult.getExitCode());
        saveAssertThat(commandResult.getCommandOutput()).matches(Pattern.compile(messagePattern, Pattern.DOTALL))
                .as(ERROR_OUTPUT_NOT_FOUND);
    }

    static List<Map<String, String>> getList(final MxeCliDriver mxeCliDriver, final CustomCommand customCommand) {
        CommandResult commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertEquals(ERROR_EXIT_CODE_NOT_ZERO, 0, commandResult.getExitCode());
        return parseListCommandResult(commandResult);
    }

    static String getFieldValue(String line, int startIndex, int endIndex) {
        if (startIndex > line.length()) {
            return null;
        }
        int n = endIndex;
        while (n > startIndex && Character.isSpaceChar(line.charAt(n))) {
            n--;
        }
        return Character.isSpaceChar(line.charAt(n)) ? null : line.substring(startIndex, n + 1);
    }

    static String getLastLine(String result) {
        int n = result.lastIndexOf('\n');
        return n > 0 ? result.substring(n + 1) : result;
    }

    /**
     * Parses the result of model list. The result looks like this:
     *
     * <pre>
     * ONBOARDED  ID                   VERSION  IMAGE_NAME                TITLE               AUTHOR    STATUS
     * 10:20      seldon.test.python   1.2.3    seldon.test.python:1.2.3  Seldon Test Python  MXE Test  available
     * 12:30      invalid-model.dummy  unknown                                                          ERROR: Something went wrong with fetching the MXE-META-INF/INFO file from the archive "invalid-model.dummy"
     * </pre>
     *
     * @param commandResult
     * @return Status field value
     */
    static List<Map<String, String>> parseListCommandResult(CommandResult commandResult) {
        List<Pair<Integer, String>> header = new ArrayList<>();
        List<Map<String, String>> listResult = new ArrayList<>();
        try (Scanner scanner = new Scanner(commandResult.getCommandOutput())) {
            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                for (String title : line.split("\\s\\s+")) {
                    header.add(Pair.of(line.indexOf(title), title));
                }
            }
            while (scanner.hasNextLine()) {
                Map<String, String> lineMap = new HashMap<>();
                String line = scanner.nextLine();
                int i = 0;
                for (Pair<Integer, String> headerElement : header) {
                    int startIndex = headerElement.getKey();
                    int endIndex = i < header.size() - 1 ? header.get(i + 1).getKey() - 1 : line.length() - 1;
                    endIndex = endIndex < startIndex || endIndex > line.length() ? line.length() - 1 : endIndex;
                    String fieldValue = MxeTestHelper.getFieldValue(line, startIndex, endIndex);
                    lineMap.put(headerElement.getValue(), fieldValue);
                    i++;
                }
                listResult.add(lineMap);
            }
        }
        return listResult;
    }

    static void sleepSecInStep(int sleepSec) {
        setTestStepBegin("Sleeping " + sleepSec + "sec");
        sleepSec(sleepSec);
        setTestStepEnd();
    }

    static void sleepSec(int sleepSec) {
        setTestInfo("Sleeping " + sleepSec + "sec");
        try {
            TimeUnit.SECONDS.sleep(sleepSec);
        } catch (InterruptedException e) {
            setTestWarning("Sleep interrupted");
            Thread.currentThread().interrupt();
        }
    }

    static String startDeployment(final MxeCliDriver mxeCliDriver, CustomCommand command, String successPattern) {
        CommandResult commandResult = mxeCliDriver.execute(command);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertEquals(ERROR_EXIT_CODE_NOT_ZERO, 0, commandResult.getExitCode());
        final Matcher matcher = Pattern.compile(successPattern).matcher(commandResult.getCommandOutput());
        assertThat(commandResult.getCommandOutput()).as(ERROR_OUTPUT_NOT_FOUND)
                .matches(Pattern.compile(successPattern, Pattern.DOTALL));
        String returnedInstanceName = null;
        if (matcher.matches()) {
            returnedInstanceName = matcher.group(1);
        }
        return returnedInstanceName;
    }

    static void waitUntilDeploymentStatusIs(MxeCliDriver mxeCliDriver, String deploymentId, String deploymentVersion,
            String expectedStatus, Duration timeOut, CustomCommand command) {
        setTestStepBegin("Check if " + deploymentId + ":" + deploymentVersion + " status is '" + expectedStatus
                + "' (timeout " + timeOut + ")");
        final long endTime = System.currentTimeMillis() + timeOut.toMillis();
        final int sleepSec = 10;

        int counter = 1;
        do {
            setSubTestStep("Check counter:" + counter++);
            MxeTestHelper.sleepSec(sleepSec);
            Optional<Map<String, String>> first = null;
            try {
                first = getList(mxeCliDriver, command).stream()
                        .filter(m -> StringUtils.equals(m.get(MxeTestHelper.HEADER_ELEMENT_ID), deploymentId))
                        .filter(m -> StringUtils.equals(m.get(HEADER_ELEMENT_VERSION), deploymentVersion)).findFirst();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (!first.isPresent()) {
                LOGGER.warn("There is no " + deploymentId + ":" + deploymentVersion);
                break;
            }

            Map<String, String> modelInfo = first.get();
            if (modelInfo.get(HEADER_ELEMENT_STATUS).startsWith(expectedStatus)) {
                setTestInfo("Status of " + deploymentId + ":" + deploymentVersion + " is '" + expectedStatus + "'");
                MxeTestHelper.sleepSec(sleepSec);
                return;
            }
            setTestInfo("Status of %s:%s is '%s', waiting some more (%s left)", deploymentId, deploymentVersion,
                    modelInfo.get(HEADER_ELEMENT_STATUS), Duration.ofMillis(endTime - System.currentTimeMillis()));

        } while (System.currentTimeMillis() < endTime);
        JcatFluentAssertApi.saveFail("Status of " + deploymentId + ":" + deploymentVersion + " have not reached '"
                + expectedStatus + "' until " + timeOut);
    }

    static void waitUntilRunningDeploymentStatusIs(MxeCliDriver mxeCliDriver, String instanceName,
            String expectedStatus, Duration timeOut, String field, CustomCommand command) {
        setTestStepBegin("Check if " + instanceName + " status is '" + expectedStatus + "' (timeout " + timeOut + ")");
        final long endTime = System.currentTimeMillis() + timeOut.toMillis();
        final int sleepSec = 10;

        int counter = 1;
        do {
            setSubTestStep("Check counter:" + counter);
            if (counter > 1) {
                MxeTestHelper.sleepSec(sleepSec);
            }
            counter++;
            Optional<Map<String, String>> first = getList(mxeCliDriver, command).stream()
                    .filter(m -> StringUtils.equals(m.get(field), instanceName)).findFirst();
            if (!first.isPresent()) {
                saveFail("There is no " + instanceName);
                return;
            }

            Map<String, String> modelInfo = first.get();
            if (StringUtils.equals(modelInfo.get(HEADER_ELEMENT_STATUS), expectedStatus)) {
                setTestInfo("Status of " + instanceName + " is '" + expectedStatus + "'");
                return;
            }
            setTestInfo("Status of %s is '%s', waiting some more (%s left)", instanceName,
                    modelInfo.get(HEADER_ELEMENT_STATUS), Duration.ofMillis(endTime - System.currentTimeMillis()));

        } while (System.currentTimeMillis() < endTime);
        saveFail("Status of " + instanceName + " have not reached '" + expectedStatus + "' until " + timeOut);
    }
}
