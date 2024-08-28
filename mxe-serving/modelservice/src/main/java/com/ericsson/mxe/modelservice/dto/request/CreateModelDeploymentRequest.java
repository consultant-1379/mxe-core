package com.ericsson.mxe.modelservice.dto.request;

import java.util.List;
import com.ericsson.mxe.modelservice.dto.AutoscalingData;
import com.ericsson.mxe.modelservice.dto.MxeModelData;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentType;
import jakarta.validation.constraints.NotNull;

public class CreateModelDeploymentRequest {
    @NotNull
    public String name;

    @NotNull
    public MxeModelDeploymentType type;

    public Integer replicas;

    public AutoscalingData autoScaling;

    @NotNull
    public List<MxeModelData> models;
}
