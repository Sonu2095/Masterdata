package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class CountryDto {

	private long id;
	private String name;
	private String two_letters;
	private String three_letters;
	private String countryCode;
	private String logCreatedBy;
	private Timestamp logCreatedOn;
	private String logUpdatedBy;

}
