package com.ericsson.mxe.jcat.chart;

import org.testng.annotations.Test;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class ChartIT {

    @Test
    public void multiColor() throws IOException {
        long now = System.currentTimeMillis();
        int samplesPerInstances = 4;
        int scaleCount = 5;
        Iterator<Integer> instances =
                IntStream.range(1, scaleCount + 1).mapToObj(i -> Collections.nCopies(samplesPerInstances, i))
                        .flatMap(List::stream).collect(Collectors.toList()).iterator();

        Axis timestampAxis = new Axis(Axis.Type.DATE, "timestamp");
        Axis reqPerSecAxis = new Axis(Axis.Type.VALUE, "req/s", false, true);
        Axis instanceAxis = new Axis(Axis.Type.VALUE, "instance", true, true);
        XYChart chart = Charts.xyChart().changeColorProperty("instance").addxAxis(timestampAxis).addyAxis(reqPerSecAxis)
                .addyAxis(instanceAxis)
                .addSeries(Series.line("Fail").colors(Palettes.blue()).yAxis(reqPerSecAxis).tooltipText("Fail {fail}")
                        .addDataField(Axis.Type.DATE, Axis.Orientation.HORIZONTAL, "timestamp")
                        .addDataField(Axis.Type.VALUE, Axis.Orientation.VERTICAL, "fail").build())
                .addSeries(Series
                        .line("Success").colors(Palettes.green()).tooltipText("Success {success}").yAxis(reqPerSecAxis)
                        .addDataField(Axis.Type.DATE, Axis.Orientation.HORIZONTAL, "timestamp").addDataField(
                                Axis.Type.VALUE, Axis.Orientation.VERTICAL, "success")
                        .build())
                .addSeries(Series.line().colors(Palettes.grey()).tooltipText("Instance: {instance}").yAxis(instanceAxis)
                        .addDataField(Axis.Type.DATE, Axis.Orientation.HORIZONTAL, "timestamp")
                        .addDataField(Axis.Type.VALUE, Axis.Orientation.VERTICAL, "instance").build())
                .data(LongStream.range(now, now + (scaleCount * samplesPerInstances)).mapToObj(ts -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("timestamp", ts);
                    m.put("success", (int) (Math.random() * 100));
                    m.put("fail", (int) (Math.random() * 100));
                    m.put("instance", instances.next());
                    return m;
                }).collect(Collectors.toList())).build();

        try (HtmlChartRender render = new HtmlChartRender(new FileOutputStream(new File("multi.html")))) {
            render.write(chart);
        }
    }

    @Test
    public void singleColor() throws IOException {
        long now = System.currentTimeMillis();

        XYChart chart = Charts.xyChart().addxAxis(new Axis(Axis.Type.DATE, "timestamp"))
                .addyAxis(new Axis(Axis.Type.VALUE, "req/s"))
                .addSeries(Series.line("Fail").colors(Arrays.asList("#ff5c33")).tooltipText("Fail: {fail}")
                        .addDataField(Axis.Type.DATE, Axis.Orientation.HORIZONTAL, "timestamp")
                        .addDataField(Axis.Type.VALUE, Axis.Orientation.VERTICAL, "fail").build())
                .addSeries(Series.line("Success").colors(Arrays.asList("#33ffc9")).tooltipText("Success: {success}")
                        .addDataField(Axis.Type.DATE, Axis.Orientation.HORIZONTAL, "timestamp")
                        .addDataField(Axis.Type.VALUE, Axis.Orientation.VERTICAL, "success").build())
                .data(LongStream.range(now, now + 10).mapToObj(ts -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("timestamp", ts);
                    m.put("success", (int) (Math.random() * 10));
                    m.put("fail", (int) (Math.random() * 10));
                    return m;
                }).collect(Collectors.toList())).build();

        try (HtmlChartRender render = new HtmlChartRender(new FileOutputStream(new File("single.html")))) {
            render.write(chart);
        }
    }
}
