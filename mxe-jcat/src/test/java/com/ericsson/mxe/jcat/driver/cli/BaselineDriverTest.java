package com.ericsson.mxe.jcat.driver.cli;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.driver.cli.BaselineDriver.BaselineResult;

public class BaselineDriverTest {

    @Test
    public void testResultParseWithoutErrors() {
        String commandOutput = "Number of prediction(s): 704\n" + "Sum prediction time: 161.0876383781433\n"
                + "Sum image load time: 0.12455487251281738";
        BaselineResult baselineResult = BaselineDriver.parseResult(new CommandResult(commandOutput, 0));

        assertThat(baselineResult.failed, is(0l));
        assertThat(baselineResult.nrOfPredictions, is(704));
        assertThat(baselineResult.sumPredictionTime, closeTo(161.087, 0.01));
        assertThat(baselineResult.sumNonPredictionTime, closeTo(0.124, 0.01));
    }

    @Test
    public void testWinFormatResultParseErrors() {
        String commandOutput =
                "Failed to predict  Error when checking : expected input_1 to have shape (256, 256, 3) but got array with shape (256, 256, 1)\r\n"
                        + "Number of prediction(s):  704    \r\n" + "Sum prediction time: 161.0876383781433\r\n"
                        + "Sum image load time: 0.12455487251281738";
        BaselineResult baselineResult = BaselineDriver.parseResult(new CommandResult(commandOutput, 0));

        assertThat(baselineResult.failed, is(1l));
        assertThat(baselineResult.nrOfPredictions, is(704));
        assertThat(baselineResult.sumPredictionTime, closeTo(161.087, 0.01));
        assertThat(baselineResult.sumNonPredictionTime, closeTo(0.124, 0.01));
    }
}
