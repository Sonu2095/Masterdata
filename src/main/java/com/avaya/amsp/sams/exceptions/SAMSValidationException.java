package com.avaya.amsp.sams.exceptions;

import lombok.Data;

@Data
public class SAMSValidationException extends RuntimeException{

    private final ValidationErrorCodes errorCode;

    public SAMSValidationException(ValidationErrorCodes code) {
        this.errorCode=code;
    }

}
