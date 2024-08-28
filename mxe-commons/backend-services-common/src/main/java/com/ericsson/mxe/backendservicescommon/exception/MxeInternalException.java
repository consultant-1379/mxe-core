package com.ericsson.mxe.backendservicescommon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "MXE internal error")
public class MxeInternalException extends MxeRuntimeException {

    private static final long serialVersionUID = 8447186770336901469L;

    public MxeInternalException() {
        super();
    }

    public MxeInternalException(Throwable cause) {
        super(cause);
    }

    public MxeInternalException(String message) {
        super(message);
    }

    public MxeInternalException(String message, Throwable cause) {
        super(message, cause);
    }
}
