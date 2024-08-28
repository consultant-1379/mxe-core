package com.ericsson.mxe.jcat.driver;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.testng.annotations.BeforeClass;
import com.ericsson.mxe.jcat.config.TestExecutionHost;
import com.ericsson.mxe.jcat.config.User;
import com.ericsson.mxe.jcat.driver.cli.LocalMxeCliDriver;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.profile.TestExecutionHostProfiles;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
@ContextConfiguration(classes = TestExecutionHostProfiles.class, loader = AnnotationConfigContextLoader.class)
@ActiveProfiles(profiles = TestExecutionHostProfiles.LOCAL)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class AutoWiredApplicationContextLocalTestExecutionHostProfile {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AutoWiredApplicationContextLocalTestExecutionHostProfile.class);

    @Rule
    public TestName testName = new TestName();

    @Autowired
    protected ApplicationContext applicationContext;

    @BeforeClass
    public static void beforeClassSetup() {
        LOGGER.info("[testClass: {}, commandProfile: {}]",
                AutoWiredApplicationContextLocalTestExecutionHostProfile.class.getSimpleName(),
                TestExecutionHostProfiles.LOCAL);
    }

    @Before
    public void beforeMethod() {
        LOGGER.info("[testMethod: {}]", testName.getMethodName());
    }

    @Test
    public void testContextShouldInitProperMxeCliDriverType() {
        final TestExecutionHost testExecutionHost = mock(TestExecutionHost.class);
        final User user = mock(User.class);

        when(testExecutionHost.getHost()).thenReturn("");
        when(testExecutionHost.getUser()).thenReturn(user);

        assertThat(applicationContext.getBean(MxeCliDriver.class, testExecutionHost),
                instanceOf(LocalMxeCliDriver.class));
    }

}
