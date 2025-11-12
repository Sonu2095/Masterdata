package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class DeviceTypeDto {
	private Long id;
	private String name;
	private String description;
	private String logCreatedBy;
	private Timestamp logCreatedOn;
	private String logUpdatedBy;
	private Timestamp logUpdatedOn;
	private long clusterItemId;

}
