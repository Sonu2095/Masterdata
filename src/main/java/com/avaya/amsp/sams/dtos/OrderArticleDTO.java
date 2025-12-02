/**
 * 
 */
package com.avaya.amsp.sams.dtos;

import com.avaya.amsp.domain.enums.StatusCode;
import lombok.Data;
import lombok.ToString;

/**
 * 
 */
@Data
@ToString
public class OrderArticleDTO {

	private String orderId;
	private Long articleClusterId;

	private Long poolId;
	private Long clusterId;

	private Integer quantity;
	private String articleAddInfo;
	private String articleRemark;
	
	private Boolean shippingReq;
	private Boolean assemblyReq;
	private Boolean automatedInstall;
	
	private StatusCode statusCode;
	private boolean markedForRemoval;
	
	private Boolean updated;
	
}
