package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class OrderDto {
	private Long id;
	private Long quantity;
	private String description;
	private String department;
	private String costCenter;
	private Timestamp orderDate;
	private String logCreatedBy;
	private Timestamp logCreatedOn;
	private String logUpdatedBy;
	private Timestamp logUpdatedOn;
//	private Set<ArticleOrder> articleOrder;
//	private ClusterConnection clusterConnection;
//	private OrderDateType orderDateType;
	private String orderStatus;
//	private OrderType orderType;
	private SiteDto site;
	private ClusterDto cluster;

}
