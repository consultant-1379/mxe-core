package com.ericsson.mxe.modeltrainingservice.persistence.domain;

import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import com.ericsson.mxe.backendservicescommon.dto.status.PackageStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractPackageEntity {
    @Column
    private String title;

    @Column
    private String author;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime created;

    @Column
    @Lob
    private String icon;

    @Enumerated
    @Column(nullable = false, columnDefinition = "smallint")
    private PackageStatus status;

    @Column
    private String message;

    @Column
    @Lob
    private String errorLog;

    @Column(nullable = false)
    private boolean internal;

    @Column
    private String createdByUserId;

    @Column
    private String createdByUserName;

    @Column
    @Lob
    private String signedByPublicKey;

    @Column
    private String signedByName;


    AbstractPackageEntity() {}

    AbstractPackageEntity(final String title, final String author, final String image, final OffsetDateTime created,
            final String icon, final PackageStatus status, final String message, final String errorLog,
            final boolean internal, final String createdByUserId, final String createdByUserName,
            final String signedByPublicKey, final String signedByName) {
        this.title = title;
        this.author = author;
        this.image = image;
        this.created = created;
        this.icon = icon;
        this.status = status;
        this.message = message;
        this.errorLog = errorLog;
        this.internal = internal;
        this.createdByUserId = createdByUserId;
        this.createdByUserName = createdByUserName;
        this.signedByName = toNullIfEmpty(signedByName);
        this.signedByPublicKey = toNullIfEmpty(signedByPublicKey);
    }

    private String toNullIfEmpty(String s) {
        if (s == null)
            return null;
        if (s.isBlank())
            return null;
        return s;
    }

    public String getImage() {
        return image;
    }

    public void setImage(final String image) {
        this.image = image;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(final OffsetDateTime created) {
        this.created = created;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(final String icon) {
        this.icon = icon;
    }

    public PackageStatus getStatus() {
        return status;
    }

    public void setStatus(final PackageStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getErrorLog() {
        return errorLog;
    }

    public void setErrorLog(final String errorLog) {
        this.errorLog = errorLog;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
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

    public void setAuthor(final String author) {
        this.author = author;
    }

    public Boolean isInternal() {
        return internal;
    }

    public void setInternal(final Boolean internal) {
        this.internal = internal;
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
}
