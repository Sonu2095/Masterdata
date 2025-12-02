package com.avaya.amsp.sams.dtos;

import lombok.Data;

@Data
public class PbxSystemDto {
    private Long id;
    private boolean sfbsSystem;
    private boolean teamsSystem;
}
