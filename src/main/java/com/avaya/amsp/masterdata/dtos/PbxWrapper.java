package com.avaya.amsp.masterdata.dtos;

import com.avaya.amsp.domain.PbxSystem;
import lombok.Data;

import java.util.List;

@Data
public class PbxWrapper {
    long clusterId;
    int pbxSystemCount;
    List<SiteDto> sites;
    List<PbxSystemDto> pbxSystemDtos;
}
