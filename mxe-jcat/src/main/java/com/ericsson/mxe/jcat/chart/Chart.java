package com.ericsson.mxe.jcat.chart;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class Chart {

    protected final String type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected final Container bottomAxesContainer;

    public Chart(String type) {
        this.type = type;
        this.bottomAxesContainer = new Container(null);
    }

    public String getType() {
        return type;
    }

    public Container getBottomAxesContainer() {
        return bottomAxesContainer;
    }

}
