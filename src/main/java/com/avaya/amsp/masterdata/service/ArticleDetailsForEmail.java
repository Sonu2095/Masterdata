/**
 * 
 */
package com.avaya.amsp.masterdata.service;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 
 */
@Data
@RequiredArgsConstructor
class ArticleDetailsForEmail {
	
	@NotNull
	final String articleName;
	@NotNull
	final String additionalInfo;
	@NotNull
	final long quantity;
	@NotNull
	final String billingType;
	@NotNull
	final double saPrice;
	
	double totalPrice;
	
	public double getTotalPrice() {
		return saPrice * quantity;
	}

}
