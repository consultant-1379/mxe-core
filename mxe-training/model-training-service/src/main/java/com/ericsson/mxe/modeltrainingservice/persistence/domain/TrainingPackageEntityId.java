package com.ericsson.mxe.modeltrainingservice.persistence.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class TrainingPackageEntityId extends EntityId {

    public TrainingPackageEntityId() {
        super();
    }

    public TrainingPackageEntityId(final String id, final String version) {
        super(id, version);
    }
}
