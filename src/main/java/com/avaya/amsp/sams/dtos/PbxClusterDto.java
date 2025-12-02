package com.avaya.amsp.sams.dtos;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PbxClusterDto {
    private Long id;
    private String name;
    private String areacode;
    private String pbxId;
    private String descriptionEnglish;
    private String areacodeInfo;
    private String logCreatedBy;
    private LocalDateTime logCreatedOn;
    private String logUpdatedBy;
    private LocalDateTime logUpdatedOn;
    private ClusterDto clusterItem;
    private String clusterKey;
    private String countryName;
    private String countryCode;
    private String userStamp;
    private Timestamp timeStamp;
}
