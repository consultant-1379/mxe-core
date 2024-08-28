package com.ericsson.mxe.jcat.test;

import org.apache.commons.lang.StringEscapeUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.ericsson.mxe.jcat.driver.gui.GuiDriver;
import com.ericsson.mxe.jcat.driver.gui.GuiDriver.BrowserType;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import com.google.common.io.Closer;
import se.ericsson.jcat.fw.annotations.JcatMethod;

public class MxeGuiTest extends MxeTestBase {

    private Closer closer = Closer.create();
    private GuiDriver guiDriver;

    @BeforeClass
    @Parameters({"browserType"})
    @JcatMethod(testTag = "TEST-MXE-GUI-TEST-SETUP", testTitle = "MXE GUI test setup")
    private void setup(@org.testng.annotations.Optional("FIREFOX") BrowserType browserType) {
        guiDriver = DriverFactory.getGuiDriver(mxeCluster.getEndpoint(), browserType);
        closer.register(guiDriver);
        guiDriver.connect();
        setTestStepBegin("source");
        setTestInfo("source: ");
        guiDriver.getSreenshot();
        setTestInfo(StringEscapeUtils.escapeHtml(guiDriver.getPageSource()));
        setTestStepEnd();
        setTestStepBegin("source text");
        guiDriver.login(mxeCluster.getMxeUser().getUserName(), mxeCluster.getMxeUser().getPassword());
        guiDriver.sleep(20);
        setTestInfo(StringEscapeUtils.escapeHtml(guiDriver.getPageSource()));
        guiDriver.getSreenshot();
        setTestStepEnd();
    }

    @Test
    @JcatMethod(testTag = "TEST-MXE-GUI-LOGIN", testTitle = "Login to MXE GUI")
    public void logoutAndLoginTest() {
        setTestStepBegin("source");
        setTestInfo(StringEscapeUtils.escapeHtml(guiDriver.getPageSource()));
        setTestStepEnd();
        guiDriver.logout();
        guiDriver.login(mxeCluster.getMxeUser().getUserName(), mxeCluster.getMxeUser().getPassword());
        guiDriver.sleep(5);
    }

    @Test
    @JcatMethod(testTag = "TEST-MXE-GUI-MODELPAGE", testTitle = "Show Model page")
    public void showModelPageTest() {
        guiDriver.openModelPage();
    }
}
