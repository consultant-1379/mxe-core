package com.ericsson.mxe.backendservicescommon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Action with MXE resource is forbidden")
public class MxeForbiddenException extends MxeRuntimeException {

    private static final long serialVersionUID = -2542873800075806840L;

    public MxeForbiddenException() {
        super();
    }

    public MxeForbiddenException(Throwable cause) {
        super(cause);
    }

    public MxeForbiddenException(String message) {
        super(message);
    }
}
