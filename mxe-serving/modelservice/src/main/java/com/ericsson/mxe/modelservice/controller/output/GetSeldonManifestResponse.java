package com.ericsson.mxe.modelservice.controller.output;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.ericsson.mxe.backendservicescommon.dto.Views;
import com.ericsson.mxe.modelservice.dto.AutoscalingData;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentStatus;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentType;
import com.ericsson.mxe.modelservice.dto.MxeModelDetails;
import com.ericsson.mxe.securitycommon.accesscontrol.Action;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;

public class GetSeldonManifestResponse {
    public String name;
    public MxeModelDeploymentType type;
    public String created;
    public MxeModelDeploymentStatus status;
    public String message;
    public Integer replicas;
    public AutoscalingData autoScaling;
    public List<MxeModelDetails> models;
    public String createdByUserId;
    public String createdByUserName;
    @JsonView(Views.ShowPermittedActions.class)
    public Set<Action> actions;

    @JsonInclude(Include.NON_NULL)
    public Map<String, Object> seldonDeploymentJson;

    @JsonInclude(Include.NON_NULL)
    public String resourceVersion;
}
