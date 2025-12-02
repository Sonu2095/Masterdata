package com.avaya.amsp.sams.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ErrorResponse {
    int code;
    String type;
    Message message;

    @Data
    @AllArgsConstructor
    public static class Message{
        int errorID;
        String errorText;
    }

}






