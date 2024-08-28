package com.ericsson.mxe.jcat.driver;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import com.ericsson.mxe.jcat.config.HostOperatingSystemType;
import com.ericsson.mxe.jcat.config.HostType;
import com.ericsson.mxe.jcat.config.TestExecutionHost;
import com.ericsson.mxe.jcat.config.User;
import com.ericsson.mxe.jcat.driver.cli.LocalMxeCliDriver;
import com.ericsson.mxe.jcat.driver.cli.RemoteMxeCliDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;

public class DriverFactoryTest {

    private static TestExecutionHost localTestExecutionHost;
    private static TestExecutionHost remoteTestExecutionHost;
    private static User user;

    @BeforeClass
    public static void beforeClassSetup() {
        localTestExecutionHost = Mockito.mock(TestExecutionHost.class);
        when(localTestExecutionHost.getHostOperatingSystemType()).thenReturn(HostOperatingSystemType.LINUX);
        when(localTestExecutionHost.getHostType()).thenReturn(HostType.LOCAL);

        user = Mockito.mock(User.class);
        when(user.getUserName()).thenReturn("");
        when(user.getPassword()).thenReturn("");
        when(user.getPrompt()).thenReturn("");
        remoteTestExecutionHost = Mockito.mock(TestExecutionHost.class);
        when(remoteTestExecutionHost.getHostOperatingSystemType()).thenReturn(HostOperatingSystemType.LINUX);
        when(remoteTestExecutionHost.getUser()).thenReturn(user);
        when(remoteTestExecutionHost.getPort()).thenReturn(0);
        when(remoteTestExecutionHost.getHostType()).thenReturn(HostType.REMOTE);
    }

    @Test
    public void testCreateLocalCliDriver() {
        assertThat(DriverFactory.getMxeCliDriver(localTestExecutionHost), instanceOf(LocalMxeCliDriver.class));
    }

    @Test
    public void testCreateRemoteCliDriver() {
        assertThat(DriverFactory.getMxeCliDriver(remoteTestExecutionHost), instanceOf(RemoteMxeCliDriver.class));
    }
}
