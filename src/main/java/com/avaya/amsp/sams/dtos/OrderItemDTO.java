 package com.avaya.amsp.sams.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

 @Data
public class OrderItemDTO {

    @JsonProperty("AreaCode")
    String areaCode;

    @JsonProperty("Extension")
    String extension;

    @JsonProperty("Domain")
    String domain;

    @JsonProperty("UserId")
    String userId;

    @JsonProperty("Status")
    int status;

    @JsonProperty("ConnectionType")
    private ConnectionType connectionType;

     boolean completed;
     String type;

     //@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
     LocalDateTime creationDate;

     //@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
     LocalDateTime lastUpdateDate;
}
