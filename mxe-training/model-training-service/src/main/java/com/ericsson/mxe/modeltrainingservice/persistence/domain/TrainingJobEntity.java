package com.ericsson.mxe.modeltrainingservice.persistence.domain;

import java.time.OffsetDateTime;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ericsson.mxe.modeltrainingservice.dto.status.TrainingJobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "training_jobs")
public class TrainingJobEntity {

    @Id
    @Column(nullable = false)
    private String id;

    @ManyToOne
    @JoinColumns({@JoinColumn(name = "package_id", referencedColumnName = "id"),
            @JoinColumn(name = "package_version", referencedColumnName = "version"),

    })
    private TrainingPackageEntity trainingPackageEntity;

    @Enumerated
    @Column(nullable = false, columnDefinition = "smallint")
    private TrainingJobStatus status;

    @Column
    private String message;

    @Column
    @Lob
    private String errorLog;

    @Column(nullable = false)
    private OffsetDateTime created;

    @Column
    private OffsetDateTime completed;

    @Column
    private String createdByUserId;

    @Column
    private String createdByUserName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TrainingPackageEntity getTrainingPackageEntity() {
        return trainingPackageEntity;
    }

    public void setTrainingPackageEntity(TrainingPackageEntity trainingPackageEntity) {
        this.trainingPackageEntity = trainingPackageEntity;
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

    public String getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(String createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getCreatedByUserName() {
        return createdByUserName;
    }

    public void setCreatedByUserName(String createdByUserName) {
        this.createdByUserName = createdByUserName;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
