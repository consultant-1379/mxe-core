package com.ericsson.mxe.backendservicescommon.exception.advices;

import org.apache.commons.lang3.StringUtils;
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
@Order(value = 100)
public class ExceptionHandlerAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<SingleMessageResponse> handleRuntimeException(Exception e) {
        return handleException(e);
    }

    private ResponseEntity<SingleMessageResponse> handleException(Exception e) {
        LOGGER.error("Internal exception", e);

        return ResponseEntity.status(getHttpStatus(e)).body(getBody(e));
    }

    private HttpStatus getHttpStatus(Exception e) {
        final ResponseStatus annotation = e.getClass().getAnnotation(ResponseStatus.class);

        if (annotation != null) {
            return annotation.value();
        } else if (e instanceof HttpMessageConversionException) {
            return HttpStatus.BAD_REQUEST;
        } else if (e instanceof ResponseStatusException exception) {
            return (HttpStatus) exception.getStatusCode();
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private SingleMessageResponse getBody(Exception e) {
        return new SingleMessageResponse(getMessage(e));
    }

    private String getMessage(Exception e) {
        final ResponseStatus annotation = e.getClass().getAnnotation(ResponseStatus.class);

        if (e instanceof ResponseStatusException exception) {
            return exception.getReason();
        } else if (StringUtils.isNotBlank(e.getMessage())) {
            return e.getMessage();
        } else if (annotation != null) {
            return annotation.reason();
        } else {
            return "Unknown error";
        }
    }
}
