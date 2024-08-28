package com.ericsson.mxe.backendservicescommon.exception.advices;

import com.ericsson.mxe.backendservicescommon.dto.SingleMessageResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ArgumentExceptionHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public SingleMessageResponse handleException(MethodArgumentNotValidException e) {
        final StringBuilder sb = new StringBuilder();

        sb.append("The body of the request is malformed. There are the following errors:");

        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            if (StringUtils.isNotEmpty(error.getDefaultMessage())) {
                sb.append("\n  - ").append(error.getDefaultMessage());
            } else {
                sb.append("\n  - Field of \"").append(error.getField()).append("\" cannot have \"")
                        .append(error.getRejectedValue()).append("\" as value.");
            }
        }

        return new SingleMessageResponse(sb.toString());
    }
}
