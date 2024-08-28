package com.ericsson.mxe.jcat.driver.gui;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.Response;
import java.util.Map;

public class MxeFirefoxDriver extends FirefoxDriver {

    public MxeFirefoxDriver(FirefoxOptions firefoxOptions) {
        super(firefoxOptions);
    }

    public Response execute(String driverCommand, Map<String, ?> parameters) {
        return super.execute(driverCommand, parameters);
    }
}
