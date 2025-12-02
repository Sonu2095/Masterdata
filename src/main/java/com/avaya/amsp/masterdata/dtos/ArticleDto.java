package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;
import java.util.List;

import com.avaya.amsp.domain.ArticleClearingTypeEnum;
import com.avaya.amsp.domain.PartsPropertyEnum;
import com.avaya.amsp.security.constants.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonIgnoreProperties(value = { "clusterId" }, allowSetters = true)
public class ArticleDto {

	private long id;
	@NotNull
	@Size(min = 1, max = 255, message = "name should not be blank")
	private String name;
	private String nameEnglish;
	private String nameGerman;
	private String remark;
	private String valueDefault;

	@NotNull
	private Double pricePurchase_dollar;
	@NotNull
	private Double pricePurchase_euro;
	@NotNull
	private Double priceSales_dollar;
	@NotNull
	private Double priceSales_euro;

	@NotNull
	private Boolean valueReadOnly;

	@NotNull
	private Boolean singleArticle;

	@NotNull
	@Min(value = 0, message = "masterArticle should have value either 0 or 1")
	@Max(value = 1, message = "masterArticle should have value either 0 or 1")
	private int masterArticle;

	@NotNull
	@Min(value = 0, message = "hardwareFromAvaya should have value either 0 or 1")
	@Max(value = 1, message = "hardwareFromAvaya should have value either 0 or 1")
	private int hardwareFromAvaya;

	@NotNull
	@Min(value = 0, message = "subjectToAuthorization should have value either 0 or 1")
	@Max(value = 1, message = "subjectToAuthorization should have value either 0 or 1")
	private int subjectToAuthorization;

	@NotNull
	@Min(value = 0, message = "billing should have value either 0 or 1")
	@Max(value = 1, message = "billing should have value either 0 or 1")
	private int billing;

	@NotNull
	@Min(value = 0, message = "incidentArticle should have value either 0 or 1")
	@Max(value = 1, message = "incidentArticle should have value either 0 or 1")
	private int incidentArticle;

	@NotNull
	@Min(value = 0, message = "servusInterface should have value either 0 or 1")
	@Max(value = 1, message = "servusInterface should have value either 0 or 1")
	private int servusInterface;

	@NotNull
	@Min(value = 0, message = "hidden should have value either 0 or 1")
	@Max(value = 1, message = "hidden should have value either 0 or 1")
	private int hidden;

	@NotNull
	@Min(value = 0, message = "nonAvailable should have value either 0 or 1")
	@Max(value = 1, message = "nonAvailable should have value either 0 or 1")
	private int nonAvailable;

	@NotNull
	@Min(value = 0, message = "shippingAddress should have value either 0 or 1")
	@Max(value = 1, message = "shippingAddress should have value either 0 or 1")
	private int shippingAddress;

	@NotNull
	@Min(value = 0, message = "assemblingAddress should have value either 0 or 1")
	@Max(value = 1, message = "assemblingAddress should have value either 0 or 1")
	private int assemblingAddress;

	@NotNull
	@Min(value = 0, message = "poolHandling should have value either 0 or 1")
	@Max(value = 1, message = "poolHandling should have value either 0 or 1")
	private int poolHandling;

	@NotNull
	@Min(value = 0, message = "clearingAtNewConnection should have value either 0 or 1")
	@Max(value = 1, message = "clearingAtNewConnection should have value either 0 or 1")
	int clearingAtNewConnection;

	@NotNull
	@Min(value = 0, message = "clearingAtChangeMove should have value either 0 or 1")
	@Max(value = 1, message = "clearingAtChangeMove should have value either 0 or 1")
	private int clearingAtChangeMove;

	@NotNull
	@Min(value = 0, message = "clearingAtDelete should have value either 0 or 1")
	@Max(value = 1, message = "clearingAtDelete should have value either 0 or 1")
	private int clearingAtDelete;

	private int quantifier;
	private ArticleClearingTypeEnum articleClearingType;
	private Long articleCategoryId;
	private String articleCategory;

	@NotNull
	@Size(min = 1, max = 255, message = "user should not be blank")
	private String user;

	@NotNull
	private int lifeTime;
	private String serviceCode;
	private String articleWizard;
	private Long articleWizardId;
	private Timestamp logCreatedOn;
	private String logCreatedBy;
	private Timestamp logUpdatedOn;
	private String logUpdatedBy;
	private String sapAvaya;
	private String sapBosh;
	private int slaDays;
	private int slaHrs;
	private int slaMin;
	private Integer isPart;
	private PartsPropertyEnum Property;
	private boolean valueTransfer;
	private int priority;

	private List<String> availableForRoles;
	private String userStamp;
	private Timestamp timeStamp;
	private String sla;
	
}
