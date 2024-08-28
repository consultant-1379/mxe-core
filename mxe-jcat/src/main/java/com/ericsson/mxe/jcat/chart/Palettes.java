package com.ericsson.mxe.jcat.chart;

import java.util.Arrays;
import java.util.List;

public class Palettes {

    private static final List<String> RED = Arrays.asList("#bf360c", "#f57c00", "#ffc107", "#ffab91", "#fff176");
    private static final List<String> GREEN = Arrays.asList("#1b5e20", "#689f38", "#81c784", "#cddc39", "#e6ee9c");
    private static final List<String> BLUE = Arrays.asList("#1a55a4", "#3d85c6", "#5b9cff", "#9ec2e3", "#a7caff");
    private static final List<String> GREY = Arrays.asList("#212121", "#9e9e9e", "#b0bec5", "#e0e0e0", "#eeeeee");

    public static List<String> red() {
        return RED;
    }

    public static List<String> green() {
        return GREEN;
    }

    public static List<String> blue() {
        return BLUE;
    }

    public static List<String> grey() {
        return GREY;
    }
}

