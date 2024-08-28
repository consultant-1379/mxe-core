package com.ericsson.mxe.modelservice.controller.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SeldonManifestRequest {

    private SeldonManifestHolder seldonManifestHolder;
    private RequestModelData requestModelData;

    public SeldonManifestHolder getSeldonManifestHolder() {
        return seldonManifestHolder;
    }

    public void setSeldonManifestHolder(SeldonManifestHolder seldonManifestHolder) {
        this.seldonManifestHolder = seldonManifestHolder;
    }

    public RequestModelData getRequestModelData() {
        return requestModelData;
    }

    public void setRequestModelData(RequestModelData requestModelData) {
        this.requestModelData = requestModelData;
    }

}
