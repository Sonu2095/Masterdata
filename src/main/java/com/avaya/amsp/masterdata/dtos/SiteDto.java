package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SiteDto {

	private Long id;
	@NotNull(message = "name cannot be null")
	private String name;
	private String nameEnglish;
	private String nameGerman;
	@NotNull(message = "locationCode cannot be null")
	private String locationCode;
	@NotNull(message = "city cannot be null")
	private String city;
	@NotNull(message = "street cannot be null")
	private String street;
	private String remark;
	private long active;
	private String logCreatedBy;
	private Timestamp logCreatedOn;
	private String logUpdatedBy;
	private Timestamp logUpdatedOn;
	private String clusterName;
	private Long clusterId;
	private String sipDomain;
	private String routingPolicy;
	private String cmName;
	private String notes;
	private Boolean ars;
	
	private String userStamp;
	private Timestamp timeStamp;

}
