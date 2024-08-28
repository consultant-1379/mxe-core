package com.ericsson.mxe.jcat.dto.autoscaling;

import java.util.List;
import java.util.Objects;

public class AutoscalingData {
    public Integer minReplicas;
    public Integer maxReplicas;
    public AutoscalingMetric metrics;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AutoscalingData))
            return false;
        AutoscalingData that = (AutoscalingData) o;
        return minReplicas.equals(that.minReplicas) && maxReplicas.equals(that.maxReplicas)
                && metrics.equals(that.metrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minReplicas, maxReplicas, metrics);
    }
}
