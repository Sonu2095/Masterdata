package com.avaya.amsp.masterdata.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OrderReportDto {

	private String orderId;
	private String orderType;
	private String pbxCluster;
	private String phone;
	private String connection;
	private String purchaser;
	private LocalDateTime orderDate;
	private LocalDateTime orderCompletionDate;
	//private String status;
	private String cluster;
}
