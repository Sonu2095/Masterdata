package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 
 * @author yadav188 
 * This class is working as a DTO for cluster
 *
 */

@Data
public class ClusterDto {

	private long id;

	@NotNull(message = "name cannot be null")
	private String name;
	private String nameGerman;
	private String nameEnglish;

	private String remark;

	@Email(message = "Email should be valid")
	private String hotlineEmail;

	private String hotlinePhone;

	@NotNull(message = "language cannot be null")
	private String languageId;
	private String language;

	private long countryId;
	private String country;
	private long currencyId;
	private String articleCurrency;
	private long accCurrencyId;
	private String accCurrency;
	private String timeZoneId;
	private String timeZone;
	private String pinDefault;
	private long active;
	private String logCreatedBy;
	private Timestamp logCreatedOn;
	private String logUpdatedBy;
	private Timestamp logUpdatedOn;
	
	private String userStamp;
	private Timestamp timeStamp;

}
