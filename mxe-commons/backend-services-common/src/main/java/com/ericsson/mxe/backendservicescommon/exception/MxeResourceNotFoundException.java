package com.ericsson.mxe.backendservicescommon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "MXE resource not found")
public class MxeResourceNotFoundException extends MxeRuntimeException {
    private static final long serialVersionUID = 1635036932589406649L;

    public MxeResourceNotFoundException() {
        super();
    }

    public MxeResourceNotFoundException(Throwable cause) {
        super(cause);
    }

    public MxeResourceNotFoundException(String message) {
        super(message);
    }
}
