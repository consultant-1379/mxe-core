package com.ericsson.mxe.modelcatalogueservice.dto;

public enum JobType {
    Source("source"), Archive("archive"), TrainingPackageSource("training_package_source");

    private final String type;

    JobType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
