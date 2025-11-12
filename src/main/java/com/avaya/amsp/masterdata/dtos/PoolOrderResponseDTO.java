package com.avaya.amsp.masterdata.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PoolOrderResponseDTO {

	private Long orderId;
	
	private String purchaserName;
	private String workingUserName;
	private Long regionId;
	private String regionKey;
	private Long clusterId;
	private String clusterKey;
	private Long siteId;
	private String siteKey;
	private Long poolId;
	private String poolKey;
	private String contractCode;
	
	private List<PoolOrderArticleDTO> articles;
	
	private String sapRequestNum;
	private String orderNotes;
	
	private LocalDateTime purchaseTs;
	private LocalDateTime updatedTs;
	
	private LocalDate plannedShippingDate;
	private LocalDate shippingDate;
	private String shippingAddress;
	private String shippingEmail;
	private String shippingNotes;
}
