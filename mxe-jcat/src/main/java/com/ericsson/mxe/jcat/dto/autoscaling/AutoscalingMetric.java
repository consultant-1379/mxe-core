package com.ericsson.mxe.jcat.dto.autoscaling;

import java.util.Objects;

public class AutoscalingMetric {
    public AutoscalingMetricName name;
    public Integer targetAverageValue;

    public AutoscalingMetric(AutoscalingMetricName name, Integer targetAverageValue) {
        this.name = name;
        this.targetAverageValue = targetAverageValue;
    }

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

    @Override
    public String toString() {
        return name.getResourceName() + ":" + targetAverageValue + name.getResourceUnit();
    }
}
