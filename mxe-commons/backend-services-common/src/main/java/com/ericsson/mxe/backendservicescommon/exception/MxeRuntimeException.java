package com.ericsson.mxe.backendservicescommon.exception;

public abstract class MxeRuntimeException extends RuntimeException {

    public MxeRuntimeException(Throwable cause) {
        super(cause);
    }

    public MxeRuntimeException(String message) {
        super(message);
    }

    public MxeRuntimeException() {
        super();
    }

    public MxeRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
