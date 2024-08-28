package com.ericsson.mxe.modeltrainingservice.persistence.domain;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "training_packages")
public class TrainingPackageEntity extends AbstractPackageEntity {

    @EmbeddedId
    private TrainingPackageEntityId id;

    // TODO: get rid of this here and in ModelEntity, and move to AbstractPackageEntity
    // But for that type conversion has to be solved in the DB for existing ModelEntities
    @Column
    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String description;

    public TrainingPackageEntityId getId() {
        return id;
    }

    public void setId(TrainingPackageEntityId id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
