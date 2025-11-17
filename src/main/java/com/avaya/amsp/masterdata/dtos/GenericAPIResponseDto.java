package com.avaya.amsp.masterdata.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class GenericAPIResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private int status;

    public GenericAPIResponseDto() {
        this.timestamp = LocalDateTime.now();
    }

    public GenericAPIResponseDto(boolean success, String message, T data, int status) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}
