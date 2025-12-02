package com.avaya.amsp.sams.dtos;

import jakarta.validation.constraints.NotBlank;

public record SAMSGetOrderRequestDTO(@NotBlank String orderID) {
}
