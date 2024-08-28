package com.ericsson.mxe;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MxeRoleClaim {

    private final List<String> globalModelServices;
    private final List<String> globalModels;
    private final List<String> globalRoles;
    private List<MxePermission> all;
    private List<MxePermission> modelServices;
    private List<MxePermission> models;

    @JsonCreator
    public MxeRoleClaim(@JsonProperty("global") Map<String, List<String>> global,
            @JsonProperty("all") Map<String, String> all,
            @JsonProperty("model-services") Map<String, String> modelServices,
            @JsonProperty("models") Map<String, String> models) {
        this.all = all.entrySet().stream().map(MxePermission::new).collect(Collectors.toList());
        this.modelServices = modelServices.entrySet().stream().map(MxePermission::new).collect(Collectors.toList());
        this.models = models.entrySet().stream().map(MxePermission::new).collect(Collectors.toList());
        this.globalModelServices = global.get("model-services");
        this.globalModels = global.get("models");
        this.globalRoles = global.get("roles");
    }

    public List<MxePermission> getAll() {
        return all;
    }

    public List<MxePermission> getModels() {
        return models;
    }

    public List<MxePermission> getModelServices() {
        return modelServices;
    }

    public List<String> getGlobalModels() {
        return globalModels;
    }

    public List<String> getGlobalModelServices() {
        return globalModelServices;
    }

    public List<String> getGlobalRoles() {
        return globalRoles;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
