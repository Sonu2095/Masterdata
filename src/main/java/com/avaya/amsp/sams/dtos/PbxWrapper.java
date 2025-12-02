package com.avaya.amsp.sams.dtos;

import lombok.Data;

import java.util.List;

@Data
public class PbxWrapper {
    long clusterId;
    int pbxSystemCount;
    List<SiteDto> sites;
    List<PbxSystemDto> pbxSystemDtos;
}
