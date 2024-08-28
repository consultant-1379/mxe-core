package com.ericsson.mxe.jcat.driver.model;

import java.time.Duration;
import java.time.Instant;

public class ModelTrafficRecord {

    private final int numberOfSuccess;
    private final int numberOfError;
    private final double averageResponseTimeSec;
    private final double minResponseTimeSec;
    private final double maxResponseTimeSec;
    private final Instant startTime;
    private final Instant endTime;
    private final Duration scale;

    public ModelTrafficRecord(Instant startTime, Duration scale, int numberOfSuccess, int numberOfError,
            double averageResponseTimeSec, double minResponseTimeSec, double maxResponseTimeSec) {
        this.numberOfSuccess = numberOfSuccess;
        this.numberOfError = numberOfError;
        this.averageResponseTimeSec = averageResponseTimeSec;
        this.minResponseTimeSec = minResponseTimeSec;
        this.maxResponseTimeSec = maxResponseTimeSec;
        this.startTime = startTime;
        this.scale = scale;
        this.endTime = startTime.plus(scale);
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public Duration getScale() {
        return scale;
    }

    public double getMaxResponseTimeSec() {
        return maxResponseTimeSec;
    }

    public double getMinResponseTimeSec() {
        return minResponseTimeSec;
    }

    public double getRequestPerSec() {
        return (numberOfSuccess + numberOfError) / (scale.toMillis() / (double) 1000);
    }

    public double getSuccessPerSec() {
        return numberOfSuccess / (scale.toMillis() / (double) 1000);
    }

    public double getErrorPerSec() {
        return numberOfError / (scale.toMillis() / (double) 1000);
    }

    public int getNumberOfSuccess() {
        return numberOfSuccess;
    }

    public int getNumberOfError() {
        return numberOfError;
    }

    public double getAverageResponseTimeSec() {
        return averageResponseTimeSec;
    }
}
