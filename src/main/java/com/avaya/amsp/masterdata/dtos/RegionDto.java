package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegionDto {

	private long id;
	private String remark;
	
	@NotNull(message = "Name cannot be null")
	private String name;
	
	private long active;
	private String logCreatedBy;
	private Timestamp logCreatedOn;
	private String logUpdatedBy;

}
