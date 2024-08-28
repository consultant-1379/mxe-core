package com.ericsson.mxe.modelservice.modelcatalog.dto;

import com.ericsson.mxe.backendservicescommon.dto.status.PackageStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelPackageData {
    public String id;
    public String version;
    public String description;
    public String image;
    public String icon;
    public String created;
    public PackageStatus status;
    public String message;
    public Boolean internal;
    public String dockerRegistrySecretName;
}
