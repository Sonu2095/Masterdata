package com.avaya.amsp.masterdata.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetExtensionRequestDto {
    @NotNull(message = "areaCode is required")
    private Long areaCode;
    
    @NotNull(message = "extension is required")
    private String extension;
}

