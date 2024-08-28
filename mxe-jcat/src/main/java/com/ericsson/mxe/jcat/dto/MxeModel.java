package com.ericsson.mxe.jcat.dto;

public class MxeModel {

    private String id;
    private String version;
    private Double weight;

    public MxeModel(String id, String version) {
        this(id, version, null);
    }

    public MxeModel(Double weight) {
        this(null, null, weight);
    }

    public MxeModel(String id, String version, Double weight) {
        this.id = id;
        this.version = version;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public Double getWeight() {
        return weight;
    }
}
