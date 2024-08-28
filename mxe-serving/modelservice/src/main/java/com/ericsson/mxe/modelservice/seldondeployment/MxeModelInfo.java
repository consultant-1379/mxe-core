package com.ericsson.mxe.modelservice.seldondeployment;

public class MxeModelInfo {

    public MxeModelInfo(String image, String endpointType, String pullSecretName) {
        this.image = image;
        this.endpointType = endpointType;
        this.pullSecretName = pullSecretName;
    }

    public MxeModelInfo(String image, String endpointType, String pullSecretName, Double weight) {
        this(image, endpointType, pullSecretName);
        this.weight = weight;
    }

    public String image;
    public String endpointType;
    public String pullSecretName;
    public Double weight;
}
