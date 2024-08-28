package com.ericsson.mxe.jcat.command;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

public class CustomCommandTest {

    @Test
    public void shouldWorkWithMultipleSpaces() {
        CustomCommand cc = new CustomCommand("main") {

            @Override
            public String getParameter() {
                return "  param  multi space";
            }
        };
        MatcherAssert.assertThat(cc.getSyntaxAsList(), Matchers.hasSize(4));
    }
}
