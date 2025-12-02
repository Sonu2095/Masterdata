package com.avaya.amsp.masterdata.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetExtensionErrorResponseDto {
    private Integer code;
    private String type;
    private ErrorMessage message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorMessage {
        private Integer errorID;
        private String errorText;
    }
}

