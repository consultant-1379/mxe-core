package com.ericsson.mxe.jcat.driver.profile;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {TestExecutionHostProfileLocal.class, TestExecutionHostProfileRemote.class})
@SuppressWarnings("squid:S1118")
public class TestExecutionHostProfiles {

    public static final String LOCAL = "LOCAL";
    public static final String REMOTE = "REMOTE";

    public static final String DEFAULT = LOCAL;

}
