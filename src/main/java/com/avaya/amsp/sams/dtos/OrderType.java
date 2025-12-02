/**
 * 
 */
package com.avaya.amsp.sams.dtos;

/**
 * 
 */
public enum OrderType {

	ADD("Add Service"),
	DEL("Delete Service"),
	CHG("Change Service"),
	MV("Move Service"),
//	SHI("Shipping Service"),
//	ASS("Assembly Service"),
//	S_A("Shipping & Assembly"),
//	AUTO("AUTOMATED");
;	
	public final String description;
	
	OrderType(String description) {
		this.description = description;
	}
}
