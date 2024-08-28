package com.ericsson.mxe.jcat.command.profile;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {CommandProfileLinux.class, CommandProfileWindows.class})
@SuppressWarnings("squid:S1118")
public class CommandProfiles {

    public static final String LINUX = "LINUX";
    public static final String WINDOWS = "WINDOWS";

    public static final String DEFAULT = LINUX;

}
