package com.ericsson.mxe.modelservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AutoscalingMetricName {

    @JsonProperty("memoryMegaBytes")
    MEMORY_MEGA_BYTES(AutoscalingMetricResourceName.memory, AutoscalingMetricResourceUnit.Mi),

    @JsonProperty("cpuMilliCores")
    CPU_MILLI_CORES(AutoscalingMetricResourceName.cpu, AutoscalingMetricResourceUnit.m),

    @JsonProperty("unknown")
    UNKNOWN(AutoscalingMetricResourceName.unknown, AutoscalingMetricResourceUnit.unknown);

    private AutoscalingMetricResourceName resourceName;

    private AutoscalingMetricResourceUnit resourceUnit;

    public AutoscalingMetricResourceName getResourceName() {
        return resourceName;
    }

    public AutoscalingMetricResourceUnit getResourceUnit() {
        return resourceUnit;
    }

    AutoscalingMetricName(AutoscalingMetricResourceName resourceName, AutoscalingMetricResourceUnit resourceUnit) {
        this.resourceName = resourceName;
        this.resourceUnit = resourceUnit;
    }

    public static AutoscalingMetricName getAutoscalingMetricNameByResourceName(AutoscalingMetricResourceName name) {
        switch (name) {
            case cpu:
                return CPU_MILLI_CORES;
            case memory:
                return MEMORY_MEGA_BYTES;
            default:
                return UNKNOWN;
        }
    }
}
