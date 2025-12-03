package com.avaya.amsp.masterdata.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListExtensionRequestDto {
    @NotNull(message = "areaCode is required")
    private Long areaCode;
    
    @NotNull(message = "fromExtension is required")
    private String fromExtension;
    
    @NotNull(message = "toExtension is required")
    private String toExtension;
    
    @Valid
    private StatusFilter status;
    
    private Integer maxCount;
    
    private Integer skipCount;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusFilter {
        private String status;
        
        @Valid
        private StatusInfo info;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class StatusInfo {
            private String reservedBy;
            private String reservedUntilDate;
        }
    }
}

