package com.ericsson.mxe.modelservice.deployer.domain;

import java.util.List;
import com.ericsson.mxe.modelservice.dto.AutoscalingData;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentStatus;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentType;

public class MxeDeployerRequestData {
    private String domain;
    private String name;
    private MxeModelDeploymentType type;
    private String created;
    private MxeModelDeploymentStatus status;
    private String message;
    private Integer replicas;
    private AutoscalingData autoScaling;
    private List<MxeDeployerModelInfo> models;
    private List<Double> weights;
    private String createdByUserId;
    private String createdByUserName;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MxeModelDeploymentType getType() {
        return type;
    }

    public void setType(MxeModelDeploymentType type) {
        this.type = type;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public MxeModelDeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(MxeModelDeploymentStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    public AutoscalingData getAutoScaling() {
        return autoScaling;
    }

    public void setAutoScaling(AutoscalingData autoScaling) {
        this.autoScaling = autoScaling;
    }

    public List<MxeDeployerModelInfo> getModels() {
        return models;
    }

    public void setModels(List<MxeDeployerModelInfo> models) {
        this.models = models;
    }

    public List<Double> getWeights() {
        return weights;
    }

    public void setWeights(List<Double> weights) {
        this.weights = weights;
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

}
