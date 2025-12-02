package com.avaya.amsp.sams.dtos;

import java.time.Instant;
import java.time.LocalDateTime;

public record SAMSGetOrderResponseDTO(
        OrderID orderID,
        CallNumber callNumber,
        LocalDateTime creationDate,
        LocalDateTime lastUpdateDate,
        OrderStatus orderStatus,
        OrderType orderType,
        boolean completed

) {
    public record OrderID(
            String orderID
    ) {
    }

    public record CallNumber(
            String areaCode,
            String extension
    ) {
    }

    public record OrderStatus(
            String status
    ) {
    }

    public record OrderType(
            String type
    ) {
    }


}

