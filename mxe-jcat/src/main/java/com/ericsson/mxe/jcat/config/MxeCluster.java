package com.ericsson.mxe.jcat.config;

import com.ericsson.commonlibrary.cf.spi.ConfigurationData;
import java.util.List;

public interface MxeCluster extends ConfigurationData {

    TestExecutionHost getCliHost();

    String getEndpoint();

    User getMxeUser();

    List<TestExecutionHost> getNodeList();
}
