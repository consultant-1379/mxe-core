package com.ericsson.mxe.jcat.util;

import com.ericsson.mxe.jcat.command.Commands;
import com.ericsson.mxe.jcat.command.CustomCommand;
import com.ericsson.mxe.jcat.command.MxeCommand;
import com.ericsson.mxe.jcat.command.linux.LinuxCommand;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.config.MxeCluster;
import com.ericsson.mxe.jcat.config.TestExecutionHost;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import com.ericsson.mxe.jcat.dto.MxeUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.ericsson.jcat.fw.utils.JcatLogDirectory;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AccessControlUtil {

    private static final String MXE_USER = "MXE_USER";
    private static final String MXE_PASSWORD = "MXE_PASSWORD";
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessControlUtil.class);

    private AccessControlUtil() {}

    public static MxeUser getCurrentUser(MxeCluster mxeCluster, String clusterName) throws IOException {
        try (MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(mxeCluster.getCliHost())) {
            if (!createTokenFileIfNecessary(mxeCliDriver, mxeCluster.getCliHost())) {
                throw new RuntimeException("Couldn't create token file");
            }
            String tokenFilePath = getTokenFilePath(mxeCliDriver, clusterName);
            Map tokenFileContent = getTokenFileContent(mxeCliDriver, tokenFilePath);
            if (tokenFileContent.isEmpty()) {
                throw new RuntimeException("Could not read token file content from " + tokenFilePath);
            }
            String middlePartOfAccessToken = getMiddlePartOfAccessToken(tokenFileContent);
            Map accessTokenAsJson = getAccessTokenAsJson(middlePartOfAccessToken);
            String preferredUserName = "preferred_username";
            String mxeUserName = (String) accessTokenAsJson.get(preferredUserName);
            if (StringUtils.isEmpty(mxeUserName)) {
                throw new IllegalArgumentException(
                        "There is no " + preferredUserName + " value in access token: " + accessTokenAsJson);
            }
            String mxePassword = null;
            if (mxeUserName.equals(getSystemEnvironmentValue(mxeCliDriver, MXE_USER))) {
                mxePassword = getSystemEnvironmentValue(mxeCliDriver, MXE_PASSWORD);
            }
            return new MxeUser(mxeUserName, mxePassword);
        }
    }

    public static boolean relogin(MxeCluster mxeCluster, String clusterName, String userName, String password)
            throws IOException {
        try (MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(mxeCluster.getCliHost())) {
            String tokenFilePath = getTokenFilePath(mxeCliDriver, clusterName);
            if (!deleteTokenFile(mxeCliDriver, tokenFilePath)) {
                return false;
            }
            Map<String, String> environment = new HashMap<>();
            environment.put(MXE_USER, userName);
            environment.put(MXE_PASSWORD, password);
            return createTokenFileIfNecessary(mxeCliDriver, mxeCluster.getCliHost(), environment);
        }
    }

    private static String getTokenFilePath(MxeCliDriver mxeCliDriver, String clusterName) throws IOException {
        String homeDir = getSystemEnvironmentValue(mxeCliDriver, "HOME");
        return homeDir + "/.mxe/mxeTokenFile." + getClusterName(mxeCliDriver, clusterName);
    }

    private static String getJcatClustersJsonFilePath() throws IOException {
        return JcatLogDirectory.getInstance().getJcatLogDirectory() + "/../../../mxe-jcat/target/config/clusters.json";
    }

    private static String getLocalClustersJsonFilePath(MxeCliDriver mxeCliDriver) throws IOException {
        String homeDir = getSystemEnvironmentValue(mxeCliDriver, "HOME");
        return homeDir + "/.mxe/clusters.json";
    }

    private static String getSystemEnvironmentValue(MxeCliDriver mxeCliDriver, String key) {
        CustomCommand customCommand = new LinuxCommand("printenv " + key);
        CommandResult result = mxeCliDriver.execute(customCommand);
        return result.getCommandOutput();
    }

    private static String getClusterName(MxeCliDriver mxeCliDriver, String clusterName) throws IOException {
        String clusterJsonFile = getJcatClustersJsonFilePath();
        CustomCommand command = new LinuxCommand("cat " + clusterJsonFile);
        CommandResult result = mxeCliDriver.execute(command);
        if (result.getExitCode() != 0) {
            clusterJsonFile = getLocalClustersJsonFilePath(mxeCliDriver);
            command = new LinuxCommand("cat " + clusterJsonFile);
            result = mxeCliDriver.execute(command);
            if (result.getExitCode() != 0) {
                return clusterName;
            }
        }
        Map map = new ObjectMapper().readValue(result.getCommandOutput(), Map.class);
        return (String) map.get("default");
    }

    private static Map getTokenFileContent(MxeCliDriver mxeCliDriver, String tokenFilePath) throws IOException {
        CustomCommand command = new LinuxCommand("cat " + tokenFilePath);
        CommandResult result = mxeCliDriver.execute(command);
        if (result.getExitCode() != 0) {
            return Collections.emptyMap();
        }
        return new ObjectMapper().readValue(result.getCommandOutput(), Map.class);
    }

    private static boolean createTokenFileIfNecessary(MxeCliDriver mxeCliDriver, TestExecutionHost testExecutionHost) {
        MxeCommand mxeCommand = Commands.mxeModel(testExecutionHost).list();
        CommandResult result = mxeCliDriver.execute(mxeCommand);
        return result.getExitCode() == 0;
    }

    private static boolean createTokenFileIfNecessary(MxeCliDriver mxeCliDriver, TestExecutionHost testExecutionHost,
            Map<String, String> environment) {
        MxeCommand mxeCommand = Commands.mxeModel(testExecutionHost).list();
        CommandResult result = mxeCliDriver.execute(mxeCommand, environment);
        return result.getExitCode() == 0;
    }

    private static String getMiddlePartOfAccessToken(Map tokenFileContent) {
        String accesTokenString = (String) tokenFileContent.get("access_token");
        if (StringUtils.isEmpty(accesTokenString)) {
            throw new IllegalArgumentException("There is no access token in token file content: " + tokenFileContent);
        }
        String[] parts = accesTokenString.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("There is no second part in access token: " + accesTokenString);
        }
        return parts[1];
    }

    private static Map getAccessTokenAsJson(String middlePartOfAccessToken) throws IOException {
        String accessToken = new String(Base64.getDecoder().decode(middlePartOfAccessToken));
        if (StringUtils.isEmpty(accessToken)) {
            throw new IllegalArgumentException(
                    "There is no decoded access token after base64 decoding from middle part of access token: "
                            + middlePartOfAccessToken);
        }
        return new ObjectMapper().readValue(accessToken, Map.class);
    }

    private static boolean deleteTokenFile(MxeCliDriver mxeCliDriver, String tokenFilePath) {
        CustomCommand command = new LinuxCommand("rm " + tokenFilePath);
        CommandResult result = mxeCliDriver.execute(command);
        return result.getExitCode() == 0;
    }
}
