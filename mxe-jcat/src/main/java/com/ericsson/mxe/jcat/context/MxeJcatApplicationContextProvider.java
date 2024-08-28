package com.ericsson.mxe.jcat.context;

import com.ericsson.mxe.jcat.config.HostOperatingSystemType;
import com.ericsson.mxe.jcat.config.HostType;
import com.ericsson.mxe.jcat.config.TestExecutionHost;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import com.ericsson.mxe.jcat.command.profile.CommandProfiles;
import com.ericsson.mxe.jcat.driver.profile.TestExecutionHostProfiles;

public class MxeJcatApplicationContextProvider {

    private static AnnotationConfigApplicationContext applicationContext;
    private static final Class<?>[] configurationClasses = {CommandProfiles.class, TestExecutionHostProfiles.class};

    private MxeJcatApplicationContextProvider() {}

    private static void newApplicationContext() {
        applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(configurationClasses);
    }

    public static synchronized AnnotationConfigApplicationContext getApplicationContext(
            TestExecutionHost testExecutionHost) {
        final String testExecutionHostProfile =
                testExecutionHost.getHostType() == HostType.LOCAL ? TestExecutionHostProfiles.LOCAL
                        : TestExecutionHostProfiles.REMOTE;
        final String commandProfile =
                testExecutionHost.getHostOperatingSystemType() == HostOperatingSystemType.LINUX ? CommandProfiles.LINUX
                        : CommandProfiles.WINDOWS;

        return setActiveProfiles(testExecutionHostProfile, commandProfile);
    }

    public static synchronized AnnotationConfigApplicationContext setActiveProfiles(String... profiles) {
        newApplicationContext();

        applicationContext.getEnvironment().setActiveProfiles(profiles);
        applicationContext.refresh();

        return applicationContext;
    }

}
