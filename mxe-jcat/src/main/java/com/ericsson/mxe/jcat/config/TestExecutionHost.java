package com.ericsson.mxe.jcat.config;

import com.ericsson.commonlibrary.cf.spi.ConfigurationData;

public interface TestExecutionHost extends ConfigurationData {

    String getHost();

    int getPort();

    User getUser();

    HostType getHostType();

    HostOperatingSystemType getHostOperatingSystemType();

    @Override
    default Class<? extends ConfigurationData> getType() {
        return TestExecutionHost.class;
    }
}
