package com.ericsson.mxe.jcat.driver.model;

public class ModelScalabilityTrafficRecord {

    private final ModelTrafficRecord trafficRecord;
    private final int instance;

    public ModelScalabilityTrafficRecord(ModelTrafficRecord trafficRecord, int instance) {
        this.trafficRecord = trafficRecord;
        this.instance = instance;
    }

    public ModelTrafficRecord getTrafficRecord() {
        return trafficRecord;
    }

    public int getInstance() {
        return instance;
    }
}
