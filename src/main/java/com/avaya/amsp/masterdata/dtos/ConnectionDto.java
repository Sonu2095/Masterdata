package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
//@JsonIgnoreProperties(value = { "portTypesIds", "clusterId" }, allowSetters = true)
public class ConnectionDto {

	long Id;

	// @NotNull
	// long clusterId;

	// Long articleId;

	// String articleName;

	@NotNull
	@Size(min = 1, max = 45, message = "name should not be blank")
	String name;

	@NotNull
	@Min(value = 0, message = "pseudoFlag should have value either 0 or 1")
	@Max(value = 1, message = "pseudoFlag should have value either 0 or 1")
	int pinApplication;

	String description;

	// @NotNull
	Integer idBcsBunch;

	/*
	 * This will be used for input wherein we only need ids on portTypes
	 * 
	 * @NotNull
	 * 
	 * @Size(min=1)
	 */
	List<Long> portTypesIds = new ArrayList<Long>(); // This will use only for taking the port id while doing create/update connection

	// This will be used in response to get all info for types.
	// different variable are required otherwise would have required separate
	// request and response DTO.
	
	List<PortTypeDto> portTypes; // This will be use of displaying the port type details in this variable portTypes while fetching the records

	// @NotNull
	// @Size(min = 1,max = 255,message = "user should not be blank")
	String user;

	Timestamp createdOn;
	Timestamp updateddOn;

	String logCreatedBy;
	String logUpdatedBy;
	
	private int sfbTeamsConnectionFlag;
	private int migrationTosfbTeams;
	private int automationEnabled;

}
