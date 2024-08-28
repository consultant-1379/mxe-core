package com.ericsson.mxe.modelcatalogueservice.persistence.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class ModelEntityId extends EntityId {
    public ModelEntityId() {
        super();
    }

    public ModelEntityId(final String id, final String version) {
        super(id, version);
    }
}
