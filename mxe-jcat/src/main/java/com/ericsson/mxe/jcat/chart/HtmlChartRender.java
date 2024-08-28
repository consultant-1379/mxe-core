package com.ericsson.mxe.jcat.chart;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

public class HtmlChartRender implements Closeable, Flushable {

    private final OutputStream out;

    public HtmlChartRender(OutputStream out) {
        this.out = out;
    }

    private static String HTML_PART1 = "<html><script src=\"https://www.amcharts.com/lib/4/core.js\"></script>\n"
            + "<script src=\"https://www.amcharts.com/lib/4/charts.js\"></script>"
            + "<script src=\"https://www.amcharts.com/lib/4/themes/animated.js\"></script>"
            + "<div id=\"chardiv\"></div>" + "<script>" + "am4core.ready(function() {"
            + "am4core.useTheme(am4themes_animated);" + "var chart = am4core.createFromConfig(";
    private static String HTML_PART2 = ",\"chardiv\", \"";
    private static String HTML_PART3 = "\")});</script></html>";

    public void write(Chart chart) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        out.write(HTML_PART1.getBytes());
        mapper.writerWithDefaultPrettyPrinter().writeValues(out).write(chart);
        out.write(HTML_PART2.getBytes());
        out.write(chart.getType().getBytes());
        out.write(HTML_PART3.getBytes());
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        this.out.close();
    }
}
