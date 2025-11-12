package com.avaya.amsp.masterdata.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetExtensionResponseDto {
    private CallNumber callNumber;
    private ConnectionType connectionType;
    private Status status;
    private User user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CallNumber {
        private Long areaCode;
        private String extension;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionType {
        private String BCSType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private String status;
        private StatusInfo info;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusInfo {
        private String reservedBy;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant reservedUntilDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        private String domain;
        private String userID;
    }
}

