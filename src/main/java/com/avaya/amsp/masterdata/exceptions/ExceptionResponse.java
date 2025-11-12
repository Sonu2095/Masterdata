package com.avaya.amsp.masterdata.exceptions;

import java.util.List;

import lombok.Data;

@Data
public class ExceptionResponse {
	
	private final String message;
	private final List<String> details;
	private final long timeStamp;

}
