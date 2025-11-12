package com.avaya.amsp.masterdata.dtos;

import lombok.Data;

@Data
public class PoolOrderArticleDTO {
	private Long articleClusterId;
	private String name;
	private Long quantity;
	private String articleAddInfo;
	private String articleRemark;
}
