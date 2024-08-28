package com.ericsson.mxe.modeltrainingservice.dto;

import java.time.OffsetDateTime;
import java.util.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ericsson.mxe.modeltrainingservice.dto.status.TrainingJobStatus;

public class TrainingJob {

    private String id;
    private String packageId;
    private String packageVersion;
    private TrainingJobStatus status;
    private String message;
    private String errorLog;
    private OffsetDateTime created;
    private OffsetDateTime completed;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public TrainingJobStatus getStatus() {
        return status;
    }

    public void setStatus(TrainingJobStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorLog() {
        return errorLog;
    }

    public void setErrorLog(String errorLog) {
        this.errorLog = errorLog;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public OffsetDateTime getCompleted() {
        return completed;
    }

    public void setCompleted(OffsetDateTime completed) {
        this.completed = completed;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        TrainingJob trainingJob = (TrainingJob) other;

        return new EqualsBuilder().append(getId(), trainingJob.getId())
                .append(getPackageId(), trainingJob.getPackageId())
                .append(getPackageVersion(), trainingJob.getPackageVersion())
                .append(getStatus(), trainingJob.getStatus()).append(getMessage(), trainingJob.getMessage())
                .append(getErrorLog(), trainingJob.getErrorLog())
                .append(getCreated().toInstant().toEpochMilli(), trainingJob.getCreated().toInstant().toEpochMilli())
                .append(getCompleted().toInstant().toEpochMilli(),
                        trainingJob.getCompleted().toInstant().toEpochMilli())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getPackageId(), getPackageVersion(), getStatus(), getMessage(), getErrorLog(),
                getCreated().toInstant().toEpochMilli(), getCompleted().toInstant().toEpochMilli());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
