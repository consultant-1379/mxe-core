package com.ericsson.mxe.jcat.test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Parameters;
import com.ericsson.mxe.jcat.command.Commands;
import com.ericsson.mxe.jcat.config.Config;
import com.ericsson.mxe.jcat.config.MxeCluster;
import com.ericsson.mxe.jcat.config.TestExecutionHost;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import com.ericsson.mxe.jcat.driver.util.SSLUtil;
import io.kubernetes.client.openapi.ApiException;
import se.ericsson.jcat.fw.annotations.JcatMethod;
import se.ericsson.jcat.fw.fixture.testng.JcatNGSuiteListener;
import se.ericsson.jcat.fw.fixture.testng.JcatNGTestListener;
import se.ericsson.jcat.fw.testbase.JcatTestBaseFluent;

@Listeners({JcatNGSuiteListener.class, JcatNGTestListener.class})
public abstract class MxeTestBase extends JcatTestBaseFluent {

    protected TestExecutionHost testExecutionHost;
    protected MxeCluster mxeCluster;

    @JcatMethod(testTag = "CONFIG-TEST-SETUP", testTitle = "Create configuration")
    @Parameters("cluster")
    @BeforeTest
    public void setup(@org.testng.annotations.Optional("hahn081") String cluster) throws IOException, ApiException {
        initCluster(cluster);
        performFirstLogin();
    }

    private void initCluster(String cluster) throws IOException {
        setTestStepBegin("Setup for cluster: " + cluster);
        final Config config = Config.getInstance();

        Optional<MxeCluster> optionalMxeCluster = config.getMxeCluster(cluster);

        if (optionalMxeCluster.isPresent()) {
            setTestInfo("Cluster is present");
            mxeCluster = optionalMxeCluster.get();
            setTestInfo("Cluster:" + mxeCluster);
            testExecutionHost = mxeCluster.getCliHost();
            setTestInfo("TestExecutionHost:" + testExecutionHost);
        } else {
            fail("Failed to get MXE cluster");
        }

        try {
            SSLUtil.turnOffSslChecking();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            setTestError("Failed to turn off ssl checking", e);
        }
        setTestStepEnd();
    }

    private void performFirstLogin() {
        setTestStepBegin("Perform first login on cluster");
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            mxeCliDriver.execute(Commands.mxeModel(testExecutionHost).listOnboarded());
        }
        setTestStepEnd();
    }
}
