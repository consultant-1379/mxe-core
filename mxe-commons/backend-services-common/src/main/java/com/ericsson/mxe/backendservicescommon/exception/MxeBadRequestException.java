package com.ericsson.mxe.backendservicescommon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad request")
public class MxeBadRequestException extends MxeRuntimeException {

    private static final long serialVersionUID = -5377950433750669172L;

    public MxeBadRequestException(Throwable cause) {
        super(cause);
    }

    public MxeBadRequestException(String message) {
        super(message);
    }

    public MxeBadRequestException() {
        super();
    }
}
