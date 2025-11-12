package com.avaya.amsp.masterdata.dtos;

import lombok.Data;

@Data
public class CalendarClusterMappingDto {
    private Long mappingId;
    private String calendarKey;
    private Integer idCluster;
}
