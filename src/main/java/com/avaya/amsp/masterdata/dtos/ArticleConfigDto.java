package com.avaya.amsp.masterdata.dtos;

import lombok.Data;

@Data
public class ArticleConfigDto {
	private int id;
	
	private String attributeName;
	//private int forMaster;
	//private int forSlave;
	private String apiAttributeName;
	private String remark;

}
