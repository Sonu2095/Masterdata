/**
 * 
 */
package com.avaya.amsp.masterdata.dtos;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public enum PoolOrderStatusCode {
	CANCELLED(0),
	OPENED(2000),
	WAITING_SAP(3000),
	WAITING_SHIPPING(4000),
	CLOSED(5000);
	
	private static Map<String, PoolOrderStatusCode> statusCodeMap = new HashMap<>();
	
	static {
		for (PoolOrderStatusCode code : PoolOrderStatusCode.values()) {
			statusCodeMap.put(code.getValue(), code);
		}
	}
	
	private final int poolOrderStatusCode;
	
	PoolOrderStatusCode(int poolOrderStatusCode) {
		this.poolOrderStatusCode = poolOrderStatusCode;
	}
	
	public String getValue() {
		return this.poolOrderStatusCode + " - " + this.name();
	}
	
	public static PoolOrderStatusCode getPoolOrderStatusCode(String value) {
		return statusCodeMap.get(value);
	}
	
	public int getPoolOrderStatusCodeValue() {
		return this.poolOrderStatusCode;
	}

}
