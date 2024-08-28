package com.ericsson.mxe.jcat.command;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import com.ericsson.mxe.jcat.command.linux.InxiCommandLinux;
import com.ericsson.mxe.jcat.command.linux.KubectlCommandLinux;
import com.ericsson.mxe.jcat.command.linux.MxeFlowCommandLinux;
import com.ericsson.mxe.jcat.command.linux.MxeModelCommandLinux;
import com.ericsson.mxe.jcat.command.windows.KubectlCommandWindows;
import com.ericsson.mxe.jcat.command.windows.MxeFlowCommandWindows;
import com.ericsson.mxe.jcat.command.windows.MxeModelCommandWindows;
import com.ericsson.mxe.jcat.config.HostOperatingSystemType;
import com.ericsson.mxe.jcat.config.HostType;
import com.ericsson.mxe.jcat.config.TestExecutionHost;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanCreationException;

@RunWith(BlockJUnit4ClassRunner.class)
public class CommandsTest {

    private static TestExecutionHost linxuxTestExecutionHost;
    private static TestExecutionHost windowsTestExecutionHost;

    @BeforeClass
    public static void beforeClassSetup() {
        linxuxTestExecutionHost = Mockito.mock(TestExecutionHost.class);
        when(linxuxTestExecutionHost.getHostOperatingSystemType()).thenReturn(HostOperatingSystemType.LINUX);
        when(linxuxTestExecutionHost.getHostType()).thenReturn(HostType.LOCAL);

        windowsTestExecutionHost = Mockito.mock(TestExecutionHost.class);
        when(windowsTestExecutionHost.getHostOperatingSystemType()).thenReturn(HostOperatingSystemType.WINDOWS);
        when(windowsTestExecutionHost.getHostType()).thenReturn(HostType.LOCAL);
    }

    @Test
    public void testLinuxMxeFlowCommand() {
        assertThat(Commands.mxeFlow(linxuxTestExecutionHost), instanceOf(MxeFlowCommandLinux.class));
    }

    @Test
    public void testWindowsMxeFlowCommand() {
        assertThat(Commands.mxeFlow(windowsTestExecutionHost), instanceOf(MxeFlowCommandWindows.class));
    }

    @Test
    public void testLinuxMxeModelCommand() {
        assertThat(Commands.mxeModel(linxuxTestExecutionHost), instanceOf(MxeModelCommandLinux.class));
    }

    @Test
    public void testWindowsMxeModelCommand() {
        assertThat(Commands.mxeModel(windowsTestExecutionHost), instanceOf(MxeModelCommandWindows.class));
    }

    @Test
    public void testLinuxKubectlCommand() {
        assertThat(Commands.kubectl(linxuxTestExecutionHost), instanceOf(KubectlCommandLinux.class));
    }

    @Test
    public void testWindowsKubectlCommand() {
        assertThat(Commands.kubectl(windowsTestExecutionHost), instanceOf(KubectlCommandWindows.class));
    }

    @Test
    public void testLinuxInxiCommand() {
        assertThat(Commands.inxi(linxuxTestExecutionHost), instanceOf(InxiCommandLinux.class));
    }

    @Test(expected = BeanCreationException.class)
    public void testWindowsInxiCommand() {
        Commands.inxi(windowsTestExecutionHost);
    }
}
