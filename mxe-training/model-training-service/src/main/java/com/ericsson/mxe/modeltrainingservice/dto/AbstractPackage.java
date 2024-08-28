package com.ericsson.mxe.modeltrainingservice.dto;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Set;
import com.ericsson.mxe.backendservicescommon.dto.Views;
import com.ericsson.mxe.securitycommon.accesscontrol.Action;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ericsson.mxe.backendservicescommon.dto.status.PackageStatus;

public abstract class AbstractPackage {
    private String id;
    private String version;
    private String title;
    private String author;
    private String description;
    private String image;
    private OffsetDateTime created;
    private String icon;
    private PackageStatus status;
    private String message;
    private String errorLog;
    private Boolean internal;
    private String createdByUserId;
    private String createdByUserName;
    private String signedByPublicKey;
    private String signedByName;
    @JsonView(Views.ShowPermittedActions.class)
    private Set<Action> actions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public PackageStatus getStatus() {
        return status;
    }

    public void setStatus(PackageStatus status) {
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

    public Boolean isInternal() {
        return internal;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
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

    public String getSignedByPublicKey() {
        return signedByPublicKey;
    }

    public void setSignedByPublicKey(String signedByPublicKey) {
        this.signedByPublicKey = signedByPublicKey;
    }

    public String getSignedByName() {
        return signedByName;
    }

    public void setSignedByName(String signedByName) {
        this.signedByName = signedByName;
    }

    public Set<Action> getActions() {
        return actions;
    }

    public void setActions(Set<Action> actions) {
        this.actions = actions;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        AbstractPackage abstractPackage = (AbstractPackage) other;

        return new EqualsBuilder().append(getId(), abstractPackage.getId())
                .append(getVersion(), abstractPackage.getVersion()).append(getTitle(), abstractPackage.getTitle())
                .append(getAuthor(), abstractPackage.getAuthor())
                .append(getDescription(), abstractPackage.getDescription())
                .append(getImage(), abstractPackage.getImage())
                .append(getCreated().toInstant().toEpochMilli(),
                        abstractPackage.getCreated().toInstant().toEpochMilli())
                .append(getIcon(), abstractPackage.getIcon()).append(getStatus(), abstractPackage.getStatus())
                .append(getMessage(), abstractPackage.getMessage()).append(getErrorLog(), abstractPackage.getErrorLog())
                .append(isInternal(), abstractPackage.isInternal())
                .append(getCreatedByUserId(), abstractPackage.getCreatedByUserId())
                .append(getCreatedByUserName(), abstractPackage.getCreatedByUserName())
                .append(getSignedByName(), abstractPackage.getSignedByName())
                .append(getSignedByPublicKey(), abstractPackage.getSignedByPublicKey()).isEquals();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getVersion(), getTitle(), getAuthor(), getDescription(), getImage(),
                getCreated().toInstant().toEpochMilli(), getIcon(), getStatus(), getMessage(), getErrorLog(),
                isInternal(), getCreatedByUserId(), getCreatedByUserName(), getSignedByName(), getSignedByPublicKey());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
