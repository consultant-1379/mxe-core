package com.ericsson.mxe.jcat.chart;

import com.ericsson.mxe.jcat.chart.XYChart.XYChartBuilder;

public class Charts {

    private Charts() {}

    public static XYChartBuilder xyChart() {
        return XYChart.builder();
    }

}
