package com.avaya.amsp.masterdata.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PoolAssetDto {
	
	@NotNull(message = "poolId cannot be null")
	private Long poolId;
	@NotNull(message = "articleId cannot be null")
	private Long articleId;
	private Long quantity;
	private String operation;
	private String reason;
	private String userName;
}
