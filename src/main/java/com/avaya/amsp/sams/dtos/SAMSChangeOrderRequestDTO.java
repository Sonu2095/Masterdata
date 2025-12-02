package com.avaya.amsp.sams.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SAMSChangeOrderRequestDTO(
        @NotNull @Valid CallNumber callNumber,
        @NotNull @Valid ConnectionType connectionType
) {
     public record CallNumber(@NotNull String areaCode, @NotBlank String extension) {}
    public record ConnectionType(@NotBlank String BCSType) {}
   }

