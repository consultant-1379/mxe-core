package com.ericsson.mxe.modelcatalogueservice.dto.response;

public class ModelCatalogueServiceResponse {
    public String message;
    public String additionalInfo;

    public ModelCatalogueServiceResponse(final String message) {
        this.message = message;
        this.additionalInfo = null;
    }

    public ModelCatalogueServiceResponse(final String message, final String additionalInfo) {
        this.message = message;
        this.additionalInfo = additionalInfo;
    }

    public ModelCatalogueServiceResponse() {}
}
