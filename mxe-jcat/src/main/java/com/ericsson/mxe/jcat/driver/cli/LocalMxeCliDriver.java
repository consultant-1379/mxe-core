package com.ericsson.mxe.jcat.driver.cli;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import com.ericsson.commonlibrary.troubleshooter.identifiable.exeption.IdentifiableRuntimeException;
import com.ericsson.mxe.jcat.command.CustomCommand;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.config.TestExecutionHost;

public class LocalMxeCliDriver extends MxeCliDriver {

    private final ProcessExecutor processExecutor;

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalMxeCliDriver.class);

    public LocalMxeCliDriver(final TestExecutionHost testExecutionHost) {
        super(testExecutionHost);
        processExecutor = new ProcessExecutor().readOutput(true).destroyOnExit();
    }

    @Override
    public CommandResult execute(CustomCommand command) {
        return execute(command, false, Collections.emptyMap());
    }

    public CommandResult execute(CustomCommand command, Map<String, String> environment) {
        return execute(command, false, environment);
    }

    @Override
    public CommandResult executeSilent(CustomCommand command) {
        return execute(command, true, Collections.emptyMap());
    }

    private CommandResult execute(CustomCommand command, boolean silent, Map<String, String> environment) {
        CommandResult commandResult = null;
        try {
            LOGGER.info("<b>[command]</b><br/>{}", command.getSyntax());
            final ProcessResult processResult =
                    processExecutor.environment(environment).command(command.getSyntaxAsList()).execute();
            commandResult = new CommandResult(processResult.outputUTF8().trim(), processResult.getExitValue());
            if (!silent) {
                LOGGER.info("<b>[result]</b><br/>{}", commandResult.getCommandOutput());
            }
            LOGGER.info("<b>[exit code]</b><br/>{}", commandResult.getExitCode());
        } catch (InterruptedException e) {
            LOGGER.error("Process was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            LOGGER.error("Timeout occurred", e);
        } catch (Exception e) {
            LOGGER.error("Could not execute command", e);
        }

        return commandResult;
    }

    @Override
    public void close() {
        // Intentionally left blank
    }

    @Override
    public void copyTo(String localPath, String remotePath) {
        try {
            File from = new File(localPath);
            File to = new File(remotePath);
            if (from.isDirectory()) {
                FileUtils.copyDirectoryToDirectory(from, to);
            } else {
                FileUtils.copyFileToDirectory(from, to);
            }
        } catch (IOException ioe) {
            String msg = "Failed to copy " + localPath + " to " + remotePath;
            LOGGER.error(msg, ioe);
            throw new IdentifiableRuntimeException(msg, ioe);
        }
    }
}
