package com.ericsson.mxe.jcat.chart;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

public class XYChart extends Chart {

    private static final String LEGEND_ITEM =
            "<div style=\"width: 20px;height: 20px;margin: 5px;border: 1px solid rgba(0, 0, 0, .2);background: %s;\"></div><span>%s</span>";

    private List<Map<String, Object>> data;
    private List<Axis> xAxes;
    private List<Axis> yAxes;
    private List<Series> series;
    private Map<String, Object> cursor;
    private String changeColorProperty;

    private XYChart(XYChartBuilder builder) {
        super("XYChart");
        this.xAxes = builder.xAxes;
        this.yAxes = builder.yAxes;
        this.data = builder.data;
        this.series = builder.series;
        this.changeColorProperty = builder.changeColorProperty;
        bottomAxesContainer.setChildren(new ArrayList<>());

        if (changeColorProperty != null) {
            Object currentProp;
            Object prevProp = null;
            Map<String, List<Label>> labelOrder = new LinkedHashMap<>();
            for (Map<String, Object> item : data) {
                currentProp = item.get(changeColorProperty);
                if (!currentProp.equals(prevProp)) {
                    for (Series s : series) {
                        String color = s.nextColor();
                        item.put(s.getId(), color);
                        String text = s.getTitle() == null || s.getTitle().isEmpty()
                                ? changeColorProperty.substring(0, 1).toUpperCase() + changeColorProperty.substring(1)
                                        + ": " + currentProp
                                : s.getTitle() + ", " + changeColorProperty + ": " + currentProp;
                        labelOrder.putIfAbsent(s.getId(), new ArrayList<>());
                        labelOrder.get(s.getId()).add(new Label(String.format(LEGEND_ITEM, color, text)));
                    }
                }
                prevProp = currentProp;
            }
            labelOrder.forEach((seriesId, labels) -> {
                Container labelGroup = createLabelGroupParent();
                labels.forEach(label -> labelGroup.getChildren().add(label));
            });
        } else {
            Map<String, Object> firstRow = data.get(0);
            Container labelGroup = createLabelGroupParent();
            series.forEach(s -> {
                String color = s.nextColor();
                firstRow.put(s.getId(), color);
                labelGroup.getChildren().add(new Label(String.format(LEGEND_ITEM, color, s.getTitle())));
            });
        }
        cursor = new HashMap<>();
        cursor.put("behavior", "zoomX");
    }

    public List<Axis> getxAxes() {
        return xAxes;
    }

    public List<Axis> getyAxes() {
        return yAxes;
    }

    public List<Series> getSeries() {
        return series;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public Map<String, Object> getCursor() {
        return cursor;
    }

    private Container createLabelGroupParent() {
        Container parent = new Container();
        parent.setChildren(new ArrayList<>());
        parent.setLayout("horizontal");
        bottomAxesContainer.getChildren().add(parent);
        return parent;
    }

    public static XYChartBuilder builder() {
        return new XYChartBuilder();
    }

    public static class XYChartBuilder {

        private List<Map<String, Object>> data;
        private List<Axis> xAxes = new ArrayList<>();
        private List<Axis> yAxes = new ArrayList<>();
        private List<Series> series = new ArrayList<>();
        private String changeColorProperty;

        public XYChartBuilder addxAxis(Axis xAxis) {
            this.xAxes.add(xAxis);
            return this;
        }

        public XYChartBuilder addyAxis(Axis yAxis) {
            this.yAxes.add(yAxis);
            return this;
        }

        public XYChartBuilder data(List<Map<String, Object>> data) {
            this.data = data;
            return this;
        }

        public XYChartBuilder changeColorProperty(String changeColorProperty) {
            this.changeColorProperty = changeColorProperty;
            return this;
        }

        public XYChartBuilder addSeries(Series s) {
            series.add(s);
            return this;
        }

        public XYChart build() {
            return new XYChart(this);
        }
    }
}
