package com.ericsson.mxe.jcat.chart;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class Label extends Container {

    private static final String TYPE = "Label";

    @JsonInclude(Include.NON_NULL)
    private final String html;
    @JsonInclude(Include.NON_NULL)
    private final String text;
    @JsonInclude(Include.NON_NULL)
    private final String textAlign;

    public Label(String html) {
        super(TYPE);
        this.html = html;
        this.text = null;
        this.textAlign = null;
        setPadding(0, 10, 0, 10);
    }

    private Label(String text, String textAlign) {
        super(TYPE);
        this.html = null;
        this.text = text;
        this.textAlign = textAlign;
    }

    public String getHtml() {
        return html;
    }

    public String getText() {
        return text;
    }

    public String getTextAlign() {
        return textAlign;
    }

}
