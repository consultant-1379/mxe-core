package com.ericsson.mxe.jcat.driver.profile;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.ericsson.mxe.jcat.config.TestExecutionHost;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.cli.RemoteMxeCliDriver;

@Component
public class TestExecutionHostProfileRemote {

    @Bean
    @Profile(TestExecutionHostProfiles.REMOTE)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MxeCliDriver mxeCliDriver(TestExecutionHost testExecutionHost) {
        return new RemoteMxeCliDriver(testExecutionHost);
    }

}
