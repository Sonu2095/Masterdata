package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class ExportConnectionDto {
	private String clusterKey;
	private String connectionKey;	
	private String nameEnglish;	
	private String nameNatLang;
	private String remark;
	private String compatiblePortTypes;
	private String roleList;
	private String bcsBunch;
	private int pinApplication;
	private String userStamp;
	private Timestamp timeStamp;
}
