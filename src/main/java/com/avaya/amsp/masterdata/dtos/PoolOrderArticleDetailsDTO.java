package com.avaya.amsp.masterdata.dtos;

import lombok.Data;

@Data
public class PoolOrderArticleDetailsDTO {
	private Long articleClusterId;
	private String name;
	private Long quantity;
}
