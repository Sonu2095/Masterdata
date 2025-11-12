package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class PbxNumberRangeDto {

	private long id;
	private Integer pbxId;
	private int PbxClusterId;
	private int rangeFrom;
	private int rangeTo;
	private int phoneNumType;
	private String remark;
	private String logCreatedBy;
	private Timestamp logCreatedOn;
	private String logUpdatedBy;
	private Timestamp logUpdatedOn;

}
