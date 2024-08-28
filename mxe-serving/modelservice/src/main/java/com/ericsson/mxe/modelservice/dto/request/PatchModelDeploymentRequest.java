package com.ericsson.mxe.modelservice.dto.request;

import java.util.List;
import com.ericsson.mxe.modelservice.dto.AutoscalingData;
import com.ericsson.mxe.modelservice.dto.MxeModelData;

public class PatchModelDeploymentRequest {
    public Integer replicas;
    public AutoscalingData autoScaling;
    public List<MxeModelData> models;
}
