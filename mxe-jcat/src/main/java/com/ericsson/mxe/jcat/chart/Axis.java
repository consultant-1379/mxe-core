package com.ericsson.mxe.jcat.chart;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.UUID;

public class Axis {

    private final String id;
    private final Type type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Title title;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Renderer renderer;

    public Axis(Type type, String title) {
        this(type, title, false, false);
    }

    public Axis(Type type, String title, boolean opposite, boolean gridDisabled) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.title = new Title(title, "bold");
        this.renderer = opposite || gridDisabled ? new Renderer(opposite, gridDisabled) : null;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public Title getTitle() {
        return title;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public enum Type {
        VALUE("ValueAxis", new String[] {"value"}), CATEGORY("CategoryAxis",
                new String[] {"category"}), DATE("DateAxis", new String[] {"date"});

        private final String name;
        private final String[] dataFields;

        Type(String name, String[] dataFields) {
            this.name = name;
            this.dataFields = dataFields;
        }

        public String[] getDataFields() {
            return dataFields;
        }

        @JsonValue
        public String getName() {
            return name;
        }
    }

    public enum Orientation {
        VERTICAL("y"), HORIZONTAL("x");

        private final String name;

        Orientation(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class Renderer {
        private boolean opposite;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Grid grid;

        public Renderer(boolean opposite, boolean gridDisabled) {
            this.opposite = opposite;
            this.grid = gridDisabled ? new Grid(true) : null;
        }

        public boolean isOpposite() {
            return opposite;
        }

        public Grid getGrid() {
            return grid;
        }
    }

    public static class Grid {
        private boolean disabled;

        public Grid(boolean disabled) {
            this.disabled = disabled;
        }

        public boolean isDisabled() {
            return disabled;
        }
    }
}

