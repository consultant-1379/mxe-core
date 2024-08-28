package com.ericsson.mxe.jcat.command;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
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
import com.ericsson.mxe.jcat.command.linux.MxeFlowCommandLinux;
import com.ericsson.mxe.jcat.command.linux.MxeModelCommandLinux;
import com.ericsson.mxe.jcat.command.profile.CommandProfiles;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
@ContextConfiguration(classes = CommandProfiles.class, loader = AnnotationConfigContextLoader.class)
@ActiveProfiles(profiles = CommandProfiles.LINUX)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class AutoWiredApplicationContextLinuxProfileTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoWiredApplicationContextLinuxProfileTest.class);

    @Rule
    public TestName testName = new TestName();

    @Autowired
    protected ApplicationContext applicationContext;

    @BeforeClass
    public static void beforeClassSetup() {
        LOGGER.info("[testClass: {}, commandProfile: {}]",
                AutoWiredApplicationContextLinuxProfileTest.class.getSimpleName(), CommandProfiles.LINUX);
    }

    @Before
    public void beforeMethod() {
        LOGGER.info("[testMethod: {}]", testName.getMethodName());
    }

    @Test
    public void testContextShouldInitProperMxeFlowCommandType() {
        assertThat(applicationContext.getBean(MxeFlowCommand.class), instanceOf(MxeFlowCommandLinux.class));
    }

    @Test
    public void testContextShouldInitProperMxeModelCommandType() {
        assertThat(applicationContext.getBean(MxeModelCommand.class), instanceOf(MxeModelCommandLinux.class));
    }

}
