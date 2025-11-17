package com.avaya.amsp.masterdata.dtos;

import java.util.List;

import lombok.Data;

@Data
public class PhoneNumberRangeDto {
	
	private long id;
	private long pbxId;
	private long PbxClusterId;
	private String PbxClusterName;
	private int rangeFrom;
	private int rangeTo;
	private String phoneNumType;
	private String remark;
	private long locked;
	private List<Integer> availablePhoneNumbers;
	private String areaCode;
	

}
