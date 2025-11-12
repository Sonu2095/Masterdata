package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PoolDto {

	private long id;

	@NotNull(message = "poolKey cannot be null")
	private String name;

	private String contract;
	private String remark;
	private String responsibleTitle;
	private String responsibleFirstName;
	private String responsibleSurname;
	private String responsibleEmail;
	// private String user;
	private String logCreatedBy;
	private Timestamp logCreatedOn;
	private String logUpdatedBy;
	private Timestamp logUpdatedOn;
	@NotNull(message = "sitekey cannot be null")
	private Long siteId;

}
