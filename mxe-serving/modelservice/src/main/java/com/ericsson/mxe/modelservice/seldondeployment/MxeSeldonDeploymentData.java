package com.ericsson.mxe.modelservice.seldondeployment;

import java.util.List;
import com.ericsson.mxe.modelservice.dto.AutoscalingData;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentType;

public class MxeSeldonDeploymentData {
    public String name;
    public MxeModelDeploymentType type;
    public Integer replicas;
    public AutoscalingData autoScaling;
    public List<MxeModelInfo> models;
    public List<Double> weights;
    public String createdByUserId;
    public String createdByUserName;
}
