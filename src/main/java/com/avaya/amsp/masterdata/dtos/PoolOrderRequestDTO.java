/**
 * 
 */
package com.avaya.amsp.masterdata.dtos;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 
 */
@Data
public class PoolOrderRequestDTO {
	
	private Long orderId;
	@NotNull(message = "List of articles cannot be null")
	private List<PoolOrderArticleDTO> articles;

	private Long regionId;
	private Long clusterId;
	private Long siteId;
	@NotNull(message = "poolId cannot be null")
	private Long poolId;
	private String contractCode;
	private String orderNotes;
	
	private String sapRequestNum;
	
	private LocalDate plannedShippingDate;
	private Long shippingAddressId;
	private LocalDate shippingDate;
	private String shippingEmail;
	private String shippingNotes;
	
}
