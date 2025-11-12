package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class CurrencyDto {
	long id;
	private String currencyName;
	private String currencyCode;
	private String currencyCharacter;
	private String exchangeRate;
	private String exchangeRateDate;
	private String logCreatedBy;
	private Timestamp logCreatedOn;
	private String logUpdatedBy;
	private Timestamp logUpdatedOn;

}
