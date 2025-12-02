package com.avaya.amsp.sams.exceptions;

import com.avaya.amsp.sams.rest.SAMSController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.Arrays;

@ControllerAdvice( assignableTypes = {SAMSController.class} )
@Slf4j
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler( { SAMSValidationException.class})
    public ResponseEntity< Object > handleValidationException(SAMSValidationException  ex, WebRequest request) {
        ValidationErrorCodes errorCode = ex.getErrorCode();

        ErrorResponse response = new ErrorResponse();
        response.setCode(400);
        response.setType("Bad request");
        response.setMessage(new ErrorResponse.Message(errorCode.getCode(),errorCode.getMessage()));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler( Exception.class )
    public ResponseEntity< Object > handleUnknownExcption(Exception ex, WebRequest request) {
        log.error("Exception - {} ", ex);
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}