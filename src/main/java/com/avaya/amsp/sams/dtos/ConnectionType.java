package com.avaya.amsp.sams.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ConnectionType {
    @JsonProperty("BCSTypes")
    private String bcsTypes;
}
