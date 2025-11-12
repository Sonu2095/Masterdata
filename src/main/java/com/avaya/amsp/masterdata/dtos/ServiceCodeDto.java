package com.avaya.amsp.masterdata.dtos;

import lombok.Data;

@Data
public class ServiceCodeDto {
	private String serviceCode;
	private String serviceCodeDescription;
	private String serviceCodeDescriptionEngl;
	private String longText;
	private String longTextEngl;
	private String scoutKey;
	private Boolean servicecodeForConnectionFee;
	private String accountingApproach;
	private String accountingApproachEngl;
}
