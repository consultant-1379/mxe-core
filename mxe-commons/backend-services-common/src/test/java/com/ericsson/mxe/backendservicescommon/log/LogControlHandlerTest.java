/** Copyright (c) 2022 Ericsson AB. All rights reserved. */
package com.ericsson.mxe.backendservicescommon.log;

import static org.awaitility.Awaitility.await;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

public class LogControlHandlerTest {
    private static final String CLASS_NAME = "LogControlHandlerTest";
    private static final String SERVICE_LOGGER_NAME = "com.ericsson.mxe.backendservicescommon";
    private static final String CONTAINER_NAME = "eric-mxe-model-training-service";
    private static final String LOG_CONTROL_FILENAME = "logcontrol.json";
    private static final Level DEFAULT_LEVEL_NOT_SET_CASE = Level.ALL;
    private static Path tempDirPath;
    private static Path logControlFile;
    private List<LogControl> logControlEntries = new ArrayList<LogControl>();
    private static ch.qos.logback.classic.LoggerContext context;
    private static Logger loggerConfig;
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(LogControlHandlerTest.class);

    @BeforeAll
    public static void beforeClass() {
        try {
            tempDirPath = Files.createTempDirectory(CLASS_NAME);
        } catch (IOException e) {
            tempDirPath = Paths.get(".");
        }
        logControlFile = Paths.get(tempDirPath.toString(), LOG_CONTROL_FILENAME);
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        context = (ch.qos.logback.classic.LoggerContext) loggerFactory;
        loggerConfig = context.getLogger(SERVICE_LOGGER_NAME);
        startThread();
    }

    @AfterAll
    public static void afterClass() {
        logger.debug("Removing " + logControlFile.toString());
        logControlFile.toFile().delete();
        tempDirPath.toFile().delete();
    }

    @BeforeEach
    public void before(TestInfo testInfo) {
        logger.info(testInfo.getDisplayName());
        clearEntries();
        // Set to a log level other than will be set
        loggerConfig.setLevel(DEFAULT_LEVEL_NOT_SET_CASE);
    }

    @AfterEach
    public void after() {
        clearEntries();
        logger.debug("Removing " + logControlFile.toString());
        logControlFile.toFile().delete();
    }

    private void clearEntries() {
        if (logControlEntries != null) {
            logControlEntries.clear();
        }
    }

    private void addLogControlEntry(Level logLevel) {
        addLogControlEntry(CONTAINER_NAME, logLevel);
    }

    private void addLogControlEntry(String containerName, Level logLevel) {
        logControlEntries.add(LogControl.builder().container(containerName)
                .severity(LogControl.Severity.fromValue(logLevel.toString().toLowerCase())).build());
    }

    private void createLogControlJson() throws IOException {
        new ObjectMapper().writeValue(logControlFile.toFile(), logControlEntries);
    }

    private void createLogControlJson(String contents) throws IOException {
        Files.write(logControlFile, contents.getBytes());
    }

    private boolean logLevelEquals(Level expectedLogLevel) {
        Level level = loggerConfig.getLevel();
        logger.debug("Configured Log level is :" + level.toString());
        logger.debug("Expected Log level is :" + expectedLogLevel.toString());
        return expectedLogLevel.toString().equalsIgnoreCase(level.toString());
    }

    private void verifyLogLevel(Level logLevel) {
        await().atMost(60, TimeUnit.SECONDS).pollDelay(Duration.ZERO).pollInterval(200, TimeUnit.MILLISECONDS)
                .until(() -> logLevelEquals(logLevel));
    }

    private void update(String containerName, Level logLevel) throws IOException {
        clearEntries();
        addLogControlEntry(containerName, logLevel);
        createLogControlJson();
    }

    private void updateAndVerify(Level logLevel) throws IOException {
        updateAndVerify(CONTAINER_NAME, logLevel);
    }

    private void updateAndVerify(String containerName, Level logLevel) throws IOException {
        update(containerName, logLevel);
        verifyLogLevel(logLevel);
    }

    public static void startThread() {
        LogControlHandler lgc = new LogControlHandler(logControlFile.toString(), CONTAINER_NAME, SERVICE_LOGGER_NAME);
        final ExecutorService exService = Executors.newSingleThreadExecutor();
        exService.execute(new Runnable() { // or use submit to get a Future (a result of computation,
            // you'll need a Callable, rather than runnable then)
            public void run() {
                try {
                    lgc.start();
                } catch (IOException | InterruptedException e) {
                    logger.error("Error in running the thread {} ", e.getMessage());
                }
            }
        });

        // waits for termination for 30 seconds
        try {
            exService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        exService.shutdownNow();
    }

    @Test
    public void TEST_LOG_CONTROL_HANDLER_LOG_LEVEL_TOGGLING() throws Exception {
        addLogControlEntry(Level.DEBUG);
        createLogControlJson();
        verifyLogLevel(Level.DEBUG);
        updateAndVerify(Level.INFO);
        updateAndVerify(Level.DEBUG);
        // Same again
        updateAndVerify(Level.DEBUG);
        updateAndVerify(Level.ERROR);
        updateAndVerify(Level.INFO);
    }

    @Test
    public void TEST_LOG_CONTROL_HANDLER_NO_FILE() throws Exception {
        verifyLogLevel(DEFAULT_LEVEL_NOT_SET_CASE);
        updateAndVerify(Level.INFO);
        updateAndVerify(Level.DEBUG);
    }

    @Test
    public void TEST_LOG_CONTROL_HANDLER_MULTIPLE_ENTRIES() throws Exception {
        addLogControlEntry("some other container", Level.INFO);
        addLogControlEntry(Level.DEBUG);
        addLogControlEntry(Level.INFO);
        createLogControlJson();
        verifyLogLevel(Level.DEBUG);

    }

    @Test
    public void TEST_LOG_CONTROL_HANDLER_FILE_NO_ENTRIES() throws Exception {
        createLogControlJson();
        verifyLogLevel(DEFAULT_LEVEL_NOT_SET_CASE);
        updateAndVerify(Level.DEBUG);
    }

    @Test
    public void TEST_LOG_CONTROL_HANDLER_SCHEMA_VIOLATION_UNSUPPORTED_LEVEL() throws Exception {
        String contents = "[{\"container\":\"" + CONTAINER_NAME + "\",\"Level\":\"FATAL\"}]";
        createLogControlJson(contents);
        verifyLogLevel(DEFAULT_LEVEL_NOT_SET_CASE);
        updateAndVerify(Level.DEBUG);
    }

    @Test
    public void TEST_LOG_CONTROL_HANDLER_FILE_EMPTY() throws Exception {
        String contents = "";
        createLogControlJson(contents);
        verifyLogLevel(DEFAULT_LEVEL_NOT_SET_CASE);
        updateAndVerify(Level.DEBUG);
    }

    @Test
    public void TEST_LOG_CONTROL_HANDLER_ADDITIONAL_ATTRIBUTE() throws Exception {
        Level Level = ch.qos.logback.classic.Level.INFO;
        String contents = "[{\"container\":\"" + CONTAINER_NAME + "\",\"Level\":\"" + Level.toString().toLowerCase()
                + "\",\"additional\":\"something\"}]";
        createLogControlJson(contents);
        // Additional accepted
        verifyLogLevel(DEFAULT_LEVEL_NOT_SET_CASE);
        updateAndVerify(Level.DEBUG);
    }

    @Test
    public void TEST_LOG_CONTROL_HANDLER_Level_WITHOUT_QUOTES() throws Exception {
        Level Level = ch.qos.logback.classic.Level.INFO;
        String contents =
                "[{\"container\":\"" + CONTAINER_NAME + "\",\"Level\":" + Level.toString().toLowerCase() + "}]";
        createLogControlJson(contents);
        verifyLogLevel(DEFAULT_LEVEL_NOT_SET_CASE);
        updateAndVerify(Level.DEBUG);
    }

    @Test
    public void TEST_LOG_CONTROL_HANDLER_SCHEMA_VIOLATION_Level_TYPE() throws Exception {
        String contents = "[{\"container\":\"" + CONTAINER_NAME + "\",\"Level\":1}]";
        createLogControlJson(contents);
        verifyLogLevel(DEFAULT_LEVEL_NOT_SET_CASE);
        updateAndVerify(Level.DEBUG);
    }

    @Test
    public void TEST_LOG_CONTROL_HANDLER_OTHER_FILE_UPDATED() throws Exception {
        verifyLogLevel(DEFAULT_LEVEL_NOT_SET_CASE);
        Path otherFile = Paths.get(tempDirPath.toString(), "other_" + LOG_CONTROL_FILENAME);
        addLogControlEntry(Level.DEBUG);
        new ObjectMapper().writeValue(otherFile.toFile(), logControlEntries);
        Thread.sleep(1000); // wait some time for possible update
        verifyLogLevel(DEFAULT_LEVEL_NOT_SET_CASE);
        otherFile.toFile().delete();
    }

}
