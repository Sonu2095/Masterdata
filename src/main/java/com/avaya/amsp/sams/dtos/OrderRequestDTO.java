/**
 * 
 */
package com.avaya.amsp.sams.dtos;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 
 */
@Data
public class OrderRequestDTO {
	
	private String orderId;

	private int quantity;

	private Long clusterId;
	private Long connectionId;
	private Long siteId;

	private OrderType orderType;

	private String cmName;
	private Long portTypeId;
	private String areaCode;
	private String phoneNumber;

	private String pseudo;
	
	private String orderPin;

	private String orderRemark;

	private String costCenter;
	private String department;

	private String subscriber;

	private Long shippingAssemblyId;
	
	private Map<Integer, List<OrderArticleDTO>> articles;
	private Map<String, String> uploadedFiles;

	private boolean attachFilesInEmail;

}
