package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class ExportPoolDto {
	private String clusterKey;
	private String siteKey;
	private String poolKey;
	private String contract;
	private String remark;
	private String title;
	private String firstName;
	private String surname;
	private String email;
	private String userStamp;
	private Timestamp timeStamp;
}
