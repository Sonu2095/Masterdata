package com.avaya.amsp.sams.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SAMSAddOrderRequestDTO(
        @NotNull @Valid Connection connection,
        @NotNull @Valid User user
) {
    public record Connection(@NotNull @Valid CallNumber callNumber, @NotNull @Valid ConnectionType connectionType) {}
    public record CallNumber(@NotNull String areaCode, @NotBlank String extension) {}
    public record ConnectionType(@NotBlank String BCSType) {}
    public record User(@NotBlank String domain, @NotBlank String userID) {}
}

