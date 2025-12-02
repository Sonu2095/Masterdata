package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;

import com.avaya.amsp.domain.Country;
import com.avaya.amsp.domain.ShippingType;
import com.avaya.amsp.domain.Site;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ShippingAddressDto {
	private Long id;
	private String companyName;
	private String consignee;
	private String street;
	private String city;
	private String postcode;
	private String building;
	private String room;
	private String contactPartner;
	private String phone;
	private String description;
	private Long active;
	@JsonProperty("siteId")
	private Long idSite;
	private String siteName;
	private String clusterName;
	private String logCreatedBy;
	private Timestamp logCreatedOn;
	private String logUpdatedBy;
	private Timestamp logUpdatedOn;
	private CountryDto country;
	private String countryName;
	private ShippingType shippingType;
	private String userStamp;
	private Timestamp timeStamp;

}
