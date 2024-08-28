package com.ericsson.mxe.modelcatalogueservice.exception.advices;

import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import com.ericsson.mxe.backendservicescommon.dto.SingleMessageResponse;

/**
 * This class is used to provide better error response handling when throwing exceptions from a REST controller
 */
@ControllerAdvice
@Order(value = 10)
public class CatalogueExceptionHandlerAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogueExceptionHandlerAdvice.class);

    @ExceptionHandler({PSQLException.class})
    public ResponseEntity<SingleMessageResponse> handlePSQLException(Exception e) {
        LOGGER.error("DB exception", e);
        return ResponseEntity.status(getHttpStatus(e)).body(new SingleMessageResponse("Internal Data Exception"));
    }

    private HttpStatus getHttpStatus(Exception e) {
        final ResponseStatus annotation = e.getClass().getAnnotation(ResponseStatus.class);

        if (annotation != null) {
            return annotation.value();
        } else if (e instanceof HttpMessageConversionException) {
            return HttpStatus.BAD_REQUEST;
        } else if (e instanceof ResponseStatusException) {
            return (HttpStatus) ((ResponseStatusException) e).getStatusCode();
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
