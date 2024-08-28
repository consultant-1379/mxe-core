package com.ericsson.mxe.jcat.driver.cli;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.remotecli.CliBuilder;
import com.ericsson.commonlibrary.remotecli.CliFactory;
import com.ericsson.commonlibrary.remotecli.ImplSsh;
import com.ericsson.commonlibrary.remotecli.Sftp;
import com.ericsson.commonlibrary.remotecli.exceptions.RemoteCliException;
import com.ericsson.mxe.jcat.command.CustomCommand;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.config.TestExecutionHost;
import com.ericsson.mxe.jcat.config.User;

public class RemoteMxeCliDriver extends MxeCliDriver {

    private static final String NEW_LINE_REG_EX = "\n";
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteMxeCliDriver.class);

    private Cli cli;

    public RemoteMxeCliDriver(final TestExecutionHost testExecutionHost) {
        super(testExecutionHost);

        final CliBuilder builder = CliFactory.newSshBuilder().setHost(testExecutionHost.getHost())
                .setUsername(testExecutionHost.getUser().getUserName()).setPort(testExecutionHost.getPort())
                .setConnectAutoFindPromptTimeoutMillis(8 * 1000).setSendTimeoutMillis(60 * 1000)

                .setSshPtySize(300, 24, 640, 480);

        Optional<File> pubKeyFile = getPubKeyFile();
        if (pubKeyFile.isPresent()) {
            builder.setSshPublicKeyFile(pubKeyFile.get());
        } else {
            builder.setPassword(testExecutionHost.getUser().getPassword());
        }

        builder.setNewline(NEW_LINE_REG_EX);
        cli = builder.build();

        cli.setExpectedRegexPrompt(StringUtils.stripToNull(testExecutionHost.getUser().getPrompt()));
    }

    @Override
    public CommandResult execute(CustomCommand command) {
        return execute(command, false);
    }

    public CommandResult execute(CustomCommand command, Map<String, String> environment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CommandResult executeSilent(CustomCommand command) {
        return execute(command, true);
    }

    private CommandResult execute(CustomCommand command, boolean silent) {
        CommandResult commandResult = null;

        try {
            if (Objects.isNull(cli)) {
                LOGGER.error("CLI not initialized, unable to send command {}", command.getSyntax());
                return null;
            }
            if (command.getTimeoutMillis() > 0) {
                cli.setSendTimeoutMillis(command.getTimeoutMillis());
            }
            cli.connect();
            LOGGER.info("<b>[expected prompt]</b><br/>{}", cli.getExpectedRegexPrompt());

            // TODO: add proper exit status
            if (!silent) {
                LOGGER.info("<b>[command]</b><br/>{}", command.getSyntax());
            }
            String cmdOut = cli.send(command.getSyntax());
            if (!silent) {
                LOGGER.info("<b>[result]</b><br/>{}", cmdOut);
            }
            String exitCode = cli.send("echo $?", null, 2000);
            if (!silent) {
                LOGGER.info("<b>[exit code]</b><br/>{}", exitCode);
            }
            if (StringUtils.isNotBlank(exitCode)) {
                String[] split = exitCode.split("\n");
                exitCode = split[split.length - 1];
            }
            commandResult = new CommandResult(cmdOut, NumberUtils.toInt(exitCode, -1));
        } catch (final RemoteCliException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (!Objects.isNull(cli)) {
                cli.disconnect();
            }
        }
        return commandResult;
    }

    @Override
    public void close() {
        cli.disconnect();
    }

    @Override
    public void copyTo(String localPath, String remoteDir) {
        TestExecutionHost testExecutionHost = getTestExecutionHost();
        User user = testExecutionHost.getUser();

        Sftp sftp = CliFactory.newSftp(testExecutionHost.getHost(), user.getUserName(), user.getPassword(),
                testExecutionHost.getPort());

        Optional<File> pubKeyFile = getPubKeyFile();
        if (pubKeyFile.isPresent()) {
            ((ImplSsh) sftp).setSshPublicKeyFile(pubKeyFile.get());
        } else {
            ((ImplSsh) sftp).setPassword(testExecutionHost.getUser().getPassword());
        }

        try {
            sftp.connect();
            sftp.put(localPath, remoteDir);
        } finally {
            sftp.disconnect();
        }
    }

    private Optional<File> getPubKeyFile() {
        if (!StringUtils.isEmpty(getTestExecutionHost().getUser().getSshPublicKeyFile())) {
            String publicKeyFile = getTestExecutionHost().getUser().getSshPublicKeyFile();
            File file;
            if (publicKeyFile.startsWith("~")) {
                file = new File(System.getProperty("user.home") + publicKeyFile.substring(1));
            } else {
                file = new File(publicKeyFile);
            }
            return Optional.of(file);

        }
        {
            return Optional.empty();
        }
    }

}
