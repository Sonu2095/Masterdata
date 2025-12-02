package com.avaya.amsp.sams.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SAMSDelMigrateOrderDto {

    @JsonProperty("AreaCode")
    @NotBlank
    private String areaCode;

    @JsonProperty("Extension")
    @NotBlank
    private String extension;

    @JsonProperty("ConnectionType")
    @NotBlank
    private String connectionType;

    private Long connectionId;

}
