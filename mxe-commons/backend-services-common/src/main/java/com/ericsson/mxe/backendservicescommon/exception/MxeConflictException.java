package com.ericsson.mxe.backendservicescommon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "MXE resource conflicting operation")
public class MxeConflictException extends MxeRuntimeException {

    private static final long serialVersionUID = -3251195197270829520L;

    public MxeConflictException() {
        super();
    }

    public MxeConflictException(Throwable cause) {
        super(cause);
    }

    public MxeConflictException(String message) {
        super(message);
    }

    public MxeConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
