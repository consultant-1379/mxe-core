package com.ericsson.mxe.backendservicescommon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "Model package not found in model catalog")
public class MxePackageNotFoundException extends MxeRuntimeException {
    private static final long serialVersionUID = -7325771907832304869L;

    public MxePackageNotFoundException(String message) {
        super(message);
    }
}
