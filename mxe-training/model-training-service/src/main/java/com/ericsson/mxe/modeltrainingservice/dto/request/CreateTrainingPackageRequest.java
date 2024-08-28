package com.ericsson.mxe.modeltrainingservice.dto.request;

public class CreateTrainingPackageRequest extends CreateRequest {

    public CreateTrainingPackageRequest(final String id, final String version, final String title, final String author,
            final String description, final String image, final String icon, final String signedByPublicKey,
            final String signedByName) {
        super(id, version, title, author, description, image, icon, null, signedByPublicKey, signedByName);
    }

}
