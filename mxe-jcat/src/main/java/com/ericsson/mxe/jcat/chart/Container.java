package com.ericsson.mxe.jcat.chart;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class Container {

    @JsonInclude(Include.NON_NULL)
    private String type;
    @JsonInclude(Include.NON_NULL)
    private String layout;
    @JsonInclude(Include.NON_NULL)
    private List<Container> children;
    @JsonInclude(Include.NON_NULL)
    private Integer paddingTop;
    @JsonInclude(Include.NON_NULL)
    private Integer paddingRight;
    @JsonInclude(Include.NON_NULL)
    private Integer paddingBottom;
    @JsonInclude(Include.NON_NULL)
    private Integer paddingLeft;

    public Container() {
        this("Container");
    }

    public Container(String type) {
        this(type, null);
    }

    public Container(String type, List<Container> children) {
        this.type = type;
        this.children = children;
    }

    public String getType() {
        return type;
    }

    public List<Container> getChildren() {
        return children;
    }

    public void setChildren(List<Container> children) {
        this.children = children;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public void setPadding(int top, int right, int bottom, int left) {
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
        this.paddingLeft = left;
    }

    public Integer getPaddingTop() {
        return paddingTop;
    }

    public Integer getPaddingRight() {
        return paddingRight;
    }

    public Integer getPaddingBottom() {
        return paddingBottom;
    }

    public Integer getPaddingLeft() {
        return paddingLeft;
    }
}
