package com.avaya.amsp.masterdata.exceptions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.avaya.amsp.masterdata.rest.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;


@ControllerAdvice( assignableTypes = {PortTypeController.class, ConnectionController.class, ArticleController.class, PoolAssetController.class, TemplateConfigurationController.class , PbxClusterController.class, CalendarController.class, GetExtensionController.class} )
@Slf4j
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity< Object > handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                           HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ExceptionResponse response = new ExceptionResponse("Method not allowed", Arrays.asList(ex.getMessage()),
                Instant.now().getEpochSecond());
        return new ResponseEntity<>(response, status);
    }


    @Override
    protected ResponseEntity< Object > handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                    HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List< String > errorDetails = new ArrayList<>();
        errorDetails.add(ex.getMessage());
        ExceptionResponse response = new ExceptionResponse("Malformed JSON request", errorDetails,
                Instant.now().getEpochSecond());
        return new ResponseEntity<>(response, status);
    }


    @ExceptionHandler( ResourceAlreadyExistsException.class )
    public ResponseEntity< Object > handleResourceAlreadyExists(Exception ex, WebRequest request) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage(), Arrays.asList(""),
                Instant.now().getEpochSecond());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler( ResourceNotFoundException.class )
    public ResponseEntity< Object > handleResourceNotFound(Exception ex, WebRequest request) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage(), Arrays.asList(""),
                Instant.now().getEpochSecond());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler( { IllegalArgumentException.class})
    public ResponseEntity< Object > handleIllegalArgument(Exception ex, WebRequest request) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage(), Arrays.asList(""),
                Instant.now().getEpochSecond());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<String> messages = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .distinct()
                .collect(Collectors.toList());

        ExceptionResponse response = new ExceptionResponse("Validation failed",messages,
                Instant.now().getEpochSecond());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler( Exception.class )
    public ResponseEntity< Object > handleUnknownExcption(Exception ex, WebRequest request) {
        log.error("Exception - {} ", ex);
        ExceptionResponse response = new ExceptionResponse("Internal server error is occurred while processing request", Arrays.asList(""),
                Instant.now().getEpochSecond());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(InvalidCalendarException.class)
    public ResponseEntity<Object> handleInvalidCalendarException(Exception ex, WebRequest request) {
    	ExceptionResponse response = new ExceptionResponse(ex.getMessage(), Arrays.asList(""), Instant.now().getEpochSecond());
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
