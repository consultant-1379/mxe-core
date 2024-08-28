package com.ericsson.mxe.modelcatalogueservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ModelDeployment {
    private final String name;
    private final String type;
    private final String created;
    private final String status;
    private final Integer replicas;
    private final List<ModelDeploymentModelData> models;

    @JsonCreator
    public ModelDeployment(@JsonProperty("name") final String name, @JsonProperty("type") final String type,
            @JsonProperty("created") final String created, @JsonProperty("status") final String status,
            @JsonProperty("replicas") final Integer replicas,
            @JsonProperty("models") final List<ModelDeploymentModelData> models) {
        this.name = name;
        this.type = type;
        this.created = created;
        this.status = status;
        this.replicas = replicas;
        this.models = models;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getCreated() {
        return created;
    }

    public String getStatus() {
        return status;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public List<ModelDeploymentModelData> getModels() {
        return models;
    }
}
