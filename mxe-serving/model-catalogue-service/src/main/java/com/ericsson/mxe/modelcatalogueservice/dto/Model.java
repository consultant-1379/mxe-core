package com.ericsson.mxe.modelcatalogueservice.dto;

import java.util.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;

public class Model extends AbstractPackage {
    private String dockerRegistrySecretName;

    public String getDockerRegistrySecretName() {
        return dockerRegistrySecretName;
    }

    public void setDockerRegistrySecretName(String dockerRegistrySecretName) {
        this.dockerRegistrySecretName = dockerRegistrySecretName;
    }

    @Override
    public boolean equals(Object other) {
        /*
         * DateTime's equals function compares timezones too, but that's not desired, so we fall back to isEqual, which
         * does only checks that they represent the same moment or not
         */
        if (EqualsBuilder.reflectionEquals(this, other, "created")) {
            Model model = (Model) other;

            if (getCreated() != null && model.getCreated() != null) {
                return getCreated().isEqual(model.getCreated());
            }

            return getCreated() == model.getCreated();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getVersion(), getTitle(), getAuthor(), getDescription(), getImage(),
                getCreated().toInstant().toEpochMilli(), getIcon(), getStatus(), getMessage(), getErrorLog(),
                isInternal(), getCreatedByUserId(), getCreatedByUserName(), getDockerRegistrySecretName());
    }
}
