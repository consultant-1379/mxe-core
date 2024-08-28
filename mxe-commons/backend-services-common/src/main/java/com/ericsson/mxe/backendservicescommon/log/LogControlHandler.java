/** Copyright (c) 2022 Ericsson AB. All rights reserved. */
package com.ericsson.mxe.backendservicescommon.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.ericsson.mxe.backendservicescommon.utils.FileMonitor;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import lombok.Data;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;


/**
 * Log control handler. Watches log configuration file events and updates log level,
 *
 * Part of the realization of DR-D1114-LOG-051-A, D.
 *
 * In all run-time retrieval failure scenarios, the logging severity level should be kept, and a warning message should
 * be logged with the current severity level indicated.
 */
@Data
public class LogControlHandler {

    private String serviceContainerName;
    private String serviceLoggerName;
    private String logControlFile;
    private static final String DEFAULT_LOG_LEVEL = "INFO";
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(LogControlHandler.class);

    private FileMonitor monitor;
    private PropertyChangeListener listener;


    public LogControlHandler(String logControlFile, String serviceContainerName, String serviceLoggerName) {
        this.logControlFile = logControlFile;
        this.serviceContainerName = serviceContainerName;
        this.serviceLoggerName = serviceLoggerName;
    }


    /**
     * Updates log level by log4j LoggerConfig.
     *
     * @param loggerName
     * @param level
     */
    private void updateLogLevel(final String loggerName, final Level level) {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        if (!(loggerFactory instanceof LoggerContext)) {
            logger.error("LoggerFactory is not a logback LoggerContext, cannot make the log level change");
            return;
        }
        LoggerContext loggerContext = (LoggerContext) loggerFactory;

        ch.qos.logback.classic.Logger logger = loggerContext.getLogger(loggerName);
        Level oldLogLevel = logger.getLevel() == null ? null : Level.valueOf(logger.getLevel().toString());
        logger.info("Log level of {} changed from {} to {}", loggerName, oldLogLevel, level);
        logger.setLevel(level);

    }



    /**
     * Get current log level.
     *
     * @param loggerName
     * @return Log level
     */
    private Level getLogLevel(final String loggerName) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger loggerConfig = loggerContext.getLogger(loggerName);
        return loggerConfig.getLevel();
    }

    /**
     * Read log configuration file and update logging level.
     *
     * @param fileName Log configuration file
     */
    private void readAndUpdateLogLevel(final String fileName) {
        try {
            String logLevel = readLogLevelJson(fileName, this.getServiceContainerName());
            logger.debug("Log level set in {}: {}", fileName, logLevel);
            updateLogLevel(this.getServiceLoggerName(), Level.toLevel(logLevel));
        } catch (Exception e) {
            logger.warn("Failed to read log level. Current log level for container {}: {}",
                    this.getServiceContainerName(), getLogLevel(this.getServiceLoggerName()));
        }
    }

    /**
     * Get log level from configuration file.
     *
     * Read and parse log configuration file (json). Return log level for given container.
     *
     * @param fileName Log configuration file.
     * @param containerName Container name.
     * @return Log level.
     */
    private String readLogLevelJson(final String fileName, final String containerName) throws Exception {
        String logLevel = DEFAULT_LOG_LEVEL;
        boolean isRead = false;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(fileName));
            final Gson gson = new Gson();
            LogControl[] listOfContainerLogLevels = gson.fromJson(reader, LogControl[].class);

            if (listOfContainerLogLevels != null) {
                for (LogControl containerLogLevel : listOfContainerLogLevels) {
                    logger.debug(containerLogLevel.toString());
                    if (containerLogLevel.getContainer().equalsIgnoreCase(containerName)) {
                        if (containerLogLevel.getSeverity() != null) {
                            logLevel = containerLogLevel.getSeverity().value();
                            isRead = true;
                            break;
                        } else {
                            logger.warn(containerLogLevel.getContainer() + " missing severity");
                        }
                    } else {
                        logger.warn("Not the expected (" + containerName + ") container name: "
                                + containerLogLevel.getContainer());
                    }
                }
            }
        } catch (JsonIOException | JsonSyntaxException e) {
            logger.warn("Failed to parse {}: {}", fileName, e.getMessage());
        } catch (FileNotFoundException e) {
            logger.warn("{} not found!", fileName);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.warn(e.getMessage());
                }
            }
        }

        // In all run-time retrieval failure scenarios, the logging severity level should be kept,
        // and a warning message should be logged with the current severity level indicated
        if (isRead) {
            return logLevel;
        } else {
            throw new Exception("Failed to read log level");
        }
    }

    /**
     * Read initial configuration, add file system directory watcher and register event handler for log configuration.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void start() throws IOException, InterruptedException {
        logger.info("Log control handler start");

        Path fileName = Paths.get(this.logControlFile);
        Path directoryToMonitor = null;
        if (Files.isDirectory(fileName)) {
            logger.warn("Directory provided and not log configuration/control file: " + this.logControlFile);
            directoryToMonitor = fileName;
        } else {
            logger.debug("Log configuration/control file: {}", this.logControlFile);
            directoryToMonitor = fileName.getParent();
        }

        if (directoryToMonitor == null) {
            directoryToMonitor = fileName;
        }

        // Read initial configuration
        if (Files.exists(fileName)) {
            readAndUpdateLogLevel(this.logControlFile);
        } else {
            // In all run-time retrieval failure scenarios, the logging severity level should be kept,
            // and a warning message should be logged with the current severity level indicated
            logger.warn(this.logControlFile + " doesn't exist!");
        }

        if (monitor == null) {
            monitor = new FileMonitor();
        }
        if (listener == null) {
            listener = (event) -> {
                logger.debug("Event: " + ((FileMonitor.ChangedEvent) event.getNewValue()).getKind() + " File: "
                        + ((FileMonitor.ChangedEvent) event.getNewValue()).getPath().toAbsolutePath());

                // NOTE mounted kubernetes configmap file is a symlink and received events doesn't contain the filename,
                // need to act on all
                readAndUpdateLogLevel(this.logControlFile);
            };
        }

        logger.debug("Directory to monitor for file updates: " + directoryToMonitor.toString());
        monitor.subscribe(directoryToMonitor, listener, StandardWatchEventKinds.ENTRY_MODIFY);
        monitor.start();
    }

    /**
     * Stop, unregister directory watcher.
     */
    public void stop() {
        logger.info("Log control handler stop");

        if (monitor != null && listener != null) {
            monitor.unsubscribe(listener);
        }
    }
}
