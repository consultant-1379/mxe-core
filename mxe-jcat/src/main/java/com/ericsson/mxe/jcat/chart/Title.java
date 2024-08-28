package com.ericsson.mxe.jcat.chart;

public class Title {
    private String text;
    private String fontWeight;

    public Title(String text, String fontWeight) {
        this.text = text;
        this.fontWeight = fontWeight;
    }

    public String getText() {
        return text;
    }

    public String getFontWeight() {
        return fontWeight;
    }
}
