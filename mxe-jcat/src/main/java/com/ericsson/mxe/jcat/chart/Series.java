package com.ericsson.mxe.jcat.chart;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

public class Series {

    private final String id;
    private final String type;
    private final Map<String, String> dataFields;
    private final int strokeWidth;
    private final Map<String, Object> propertyFields = new HashMap<>();
    private final String tooltipText;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String yAxis;
    @JsonIgnore
    private String title;
    @JsonIgnore
    private final List<String> colors;
    @JsonIgnore
    private Iterator<String> colorsIt;

    private Series(SeriesBuilder builder) {
        this.id = UUID.randomUUID().toString();
        this.yAxis = builder.yAxis;
        this.title = builder.title;
        this.type = builder.type;
        this.dataFields = builder.dataFields;
        this.tooltipText = builder.tooltipText;
        this.colors = builder.colors;
        this.colorsIt = colors.iterator();
        strokeWidth = 3;
        propertyFields.put("stroke", this.id);
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getColors() {
        return colors;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public String nextColor() {
        return colorsIt.next();
    }

    public Map<String, String> getDataFields() {
        return dataFields;
    }

    public String getTooltipText() {
        return tooltipText;
    }

    public Map<String, Object> getPropertyFields() {
        return propertyFields;
    }

    public String getyAxis() {
        return yAxis;
    }

    public static SeriesBuilder builder(String title) {
        return new SeriesBuilder(title);
    }

    public static SeriesBuilder line() {
        return line(null);
    }

    public static SeriesBuilder line(String title) {
        return new SeriesBuilder(title).type("LineSeries");
    }

    public static class SeriesBuilder {

        private String title;
        private String type;
        private List<String> colors;
        private Map<String, String> dataFields = new HashMap<>();
        private String tooltipText = "";
        private String yAxis;

        public SeriesBuilder(String title) {
            this.title = title;
        }

        public SeriesBuilder tooltipText(String tooltipText) {
            this.tooltipText = tooltipText;
            return this;
        }

        public SeriesBuilder type(String type) {
            this.type = type;
            return this;
        }

        public SeriesBuilder addDataField(Axis.Type axisType, Axis.Orientation axisOrientation, String fieldName) {
            Arrays.stream(axisType.getDataFields()).forEach(
                    axisTypeName -> dataFields.put(axisTypeName + axisOrientation.getName().toUpperCase(), fieldName));
            return this;
        }

        public SeriesBuilder colors(List<String> colors) {
            this.colors = colors;
            return this;
        }

        public SeriesBuilder yAxis(Axis axis) {
            this.yAxis = axis.getId();
            return this;
        }

        public Series build() {
            return new Series(this);
        }
    }
}
