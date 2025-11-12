package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;

import com.avaya.amsp.domain.PoolOperationTypeEnum;

import lombok.Data;

@Data
public class ArticlePoolInventoryHistoryDto {

	private Long id;
	private String articleName;
	private Timestamp updatedTS;
	private String user;
	private PoolOperationTypeEnum operation;
	private long difference;
	private long available;
	private long reserved;
	private String reason;
	
}
