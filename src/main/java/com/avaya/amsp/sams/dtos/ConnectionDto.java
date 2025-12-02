package com.avaya.amsp.sams.dtos;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class ConnectionDto {

	private long Id;
	private String name;
	private int pinApplication;
	private String description;
	private Integer idBcsBunch;
	private String user;
	private Timestamp createdOn;
	private Timestamp updateddOn;
	private	String logCreatedBy;
	private String logUpdatedBy;
	private int sfbTeamsConnectionFlag;
	private int migrationTosfbTeams;
	private int automationEnabled;
}
