package com.ericsson.mxe.modelcatalogueservice.persistence.domain;

import java.time.OffsetDateTime;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ericsson.mxe.backendservicescommon.dto.status.PackageStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "models")
public class ModelEntity extends AbstractPackageEntity {
    @EmbeddedId
    private ModelEntityId id;

    @Column(nullable = false)
    private boolean stateful;

    @Column
    @Lob
    private String description;

    @Column
    private String dockerRegistrySecretName;

    public ModelEntity() {
        super();
    }

    public ModelEntity(final String title, final String author, final String image, final OffsetDateTime created,
            final String icon, final PackageStatus status, final String message, final String errorLog,
            final boolean internal, final String createdByUserId, final String createdByUserName,
            final ModelEntityId id, final String description, final String dockerRegistrySecretName,
            final boolean stateful) {
        this(title, author, image, created, icon, status, message, errorLog, internal, createdByUserId,
                createdByUserName, id, description, dockerRegistrySecretName, null, null, stateful);
    }


    public ModelEntity(final String title, final String author, final String image, final OffsetDateTime created,
            final String icon, final PackageStatus status, final String message, final String errorLog,
            final boolean internal, final String createdByUserId, final String createdByUserName,
            final ModelEntityId id, final String description, final String dockerRegistrySecretName,
            final String signedByPublicKey, final String signedByName, final boolean stateful) {
        super(title, author, image, created, icon, status, message, errorLog, internal, createdByUserId,
                createdByUserName, signedByPublicKey, signedByName);

        this.id = id;
        this.description = description;
        this.dockerRegistrySecretName = dockerRegistrySecretName;
        this.stateful = stateful;
    }

    public ModelEntityId getId() {
        return id;
    }

    public void setId(final ModelEntityId id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getDockerRegistrySecretName() {
        return dockerRegistrySecretName;
    }

    public void setDockerRegistrySecretName(String dockerRegistrySecretName) {
        this.dockerRegistrySecretName = dockerRegistrySecretName;
    }

    public boolean isStateful() {
        return stateful;
    }

    public void setStateful(boolean stateful) {
        this.stateful = stateful;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
