package com.avaya.amsp.masterdata.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReserveExtensionRequestDto {
    
    @NotNull(message = "callNumber is required")
    @Valid
    private CallNumber callNumber;
    
    private Integer maxReservationTimeInSecs;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CallNumber {
        @NotNull(message = "areacode is required")
        private Long areacode;
        
        @NotNull(message = "extension is required")
        private String extension;
    }
}





