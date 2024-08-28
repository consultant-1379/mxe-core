package com.ericsson.mxe.modelservice.seldondeployment;

import com.ericsson.mxe.modelservice.dto.AutoscalingData;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentStatus;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentType;
import java.util.List;

public class MxeSeldonDeploymentInfo {
    public String name;
    public MxeModelDeploymentType type;
    public String created;
    public MxeModelDeploymentStatus status;
    public String message;
    public Integer replicas;
    public AutoscalingData autoScaling;
    public List<MxeModelInfo> models;
    public String createdByUserId;
    public String createdByUserName;
    public String resourceVersion;
}
