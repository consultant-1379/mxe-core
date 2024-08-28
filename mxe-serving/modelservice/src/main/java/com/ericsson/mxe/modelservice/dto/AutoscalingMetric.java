package com.ericsson.mxe.modelservice.dto;

import java.util.Objects;

public class AutoscalingMetric {
    public AutoscalingMetricName name;
    public Integer targetAverageValue;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AutoscalingMetric))
            return false;
        AutoscalingMetric that = (AutoscalingMetric) o;
        return name == that.name && targetAverageValue.equals(that.targetAverageValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, targetAverageValue);
    }
}
