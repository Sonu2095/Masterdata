package com.avaya.amsp.sams.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class ClusterDto {
    private long id;

    private String name;
    private String nameGerman;
    private String nameEnglish;
    private String remark;
    private String hotlineEmail;
    private String hotlinePhone;
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
