package com.ericsson.mxe.jcat.driver.util;

import com.ericsson.mxe.jcat.config.TestExecutionHost;
import com.ericsson.mxe.jcat.context.MxeJcatApplicationContextProvider;
import com.ericsson.mxe.jcat.driver.cli.BaselineDriver;
import com.ericsson.mxe.jcat.driver.cli.BaselineDriver.Params;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.gui.GuiDriver;
import com.ericsson.mxe.jcat.driver.gui.GuiDriver.BrowserType;
import com.ericsson.mxe.jcat.driver.keycloak.KeycloakDriver;
import com.ericsson.mxe.jcat.driver.prometheus.PrometheusDriver;
import java.net.MalformedURLException;

public final class DriverFactory {

    private DriverFactory() {}

    public static GuiDriver getGuiDriver(String endpoint, BrowserType browserType) {
        return new GuiDriver(endpoint, browserType);
    }

    public static MxeCliDriver getMxeCliDriver(final TestExecutionHost testExecutionHost) {
        return MxeJcatApplicationContextProvider.getApplicationContext(testExecutionHost).getBean(MxeCliDriver.class,
                testExecutionHost);
    }

    public static PrometheusDriver getPrometheusDriver(final String hostName, boolean portForward)
            throws MalformedURLException {
        return new PrometheusDriver(hostName, portForward);
    }

    public static KeycloakDriver getKeycloakDriver(final String hostName, final String user, final String password,
            boolean portForward) throws MalformedURLException {
        return new KeycloakDriver(hostName, user, password, portForward);
    }

    public static BaselineDriver getBaselineDriver(final MxeCliDriver cliDriver, final Params params) {
        return new BaselineDriver(cliDriver, params);
    }

}
