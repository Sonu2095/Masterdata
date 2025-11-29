package com.avaya.amsp.masterdata.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListUserExtensionsRequestDto {
    @NotNull(message = "domain is required")
    private String domain;

    @NotNull(message = "userID is required")
    private String userID;
}

