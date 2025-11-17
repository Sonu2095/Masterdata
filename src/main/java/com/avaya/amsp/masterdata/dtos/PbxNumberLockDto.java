package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class PbxNumberLockDto {

	private Long id;
	private Long idCluster;
	private Long idPbxCluster;
	private String phoneNumber;
	private String reason;
	private boolean freePbxSync;
	private String logCreatedBy;
	private Timestamp logCreatedOn;
	private String logUpdatedBy;
	private Timestamp logUpdatedOn;

}
