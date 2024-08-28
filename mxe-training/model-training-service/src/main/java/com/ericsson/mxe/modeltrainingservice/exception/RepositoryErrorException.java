package com.ericsson.mxe.modeltrainingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RepositoryErrorException extends ResponseStatusException {
    public RepositoryErrorException(Exception cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, cause.getMessage(), cause);
    }
}
