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
class PoolArticleDetailsForEmail {
	
	@NotNull
	final String articleName;
    @NotNull 
    final long quantity;
	@NotNull
	final String saPrice;
	@NotNull
	final String puPrice;
	/*
	 * @NotNull final String currency;
	 */
	@NotNull
	final String totalSaPrice;
	@NotNull
	final String totalPuPrice;
	
	/*
	 * public double getTotalSaPrice() { return saPrice * quantity; }
	 * 
	 * public double getTotalPuPrice() { return puPrice * quantity; }
	 */

}
