package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;
import java.util.List;

import com.avaya.amsp.domain.ArticleClearingTypeEnum;
import com.avaya.amsp.domain.PartsPropertyEnum;

import lombok.Data;

@Data
public class ArticleToClusterDto {

	private Long id;
	// private Long articleId;
	// private Long clusterId;
	private String name;
	private String nameEnglish;
	private String nameGerman;
	private String remark;
	private Double pricePurchase_dollar;
	private Double pricePurchase_euro;
	private Double priceSales_dollar;
	private Double priceSales_euro;
	private String valueDefault;
	private Boolean valueReadOnly;
	private Boolean singleArticle;
	private int masterArticle;
	private int hardwareFromAvaya;
	private int subjectToAuthorization;
	private int billing;
	private int incidentArticle;
	private int servusInterface;
	private int hidden;
	private int nonAvailable;
	private int shippingAddress;
	private int assemblingAddress;
	private int poolHandling;
	private int priority;
	int clearingAtNewConnection;
	private int clearingAtChangeMove;
	private int clearingAtDelete;
	private int quantifier;
	private ArticleClearingTypeEnum articleClearingType;
	private Long articleCategoryId;
	private String articleCategory;
	private int lifeTime;
	private String serviceCode;
	private Long ArticleWizardId;
	private String articleWizard;
	private Timestamp logCreatedOn;
	private String logCreatedBy;
	private Timestamp logUpdatedOn;
	private String logUpdatedBy;
	private String sapAvaya;
	private String sapBosh;
	private String user;
	private Integer slaDays;
	private Integer slaHrs;
	private Integer slaMin;
	private Integer isPart;
	private PartsPropertyEnum Property;
	private Boolean valueTransfer;
	private Boolean isMasterPartStatus;
	private String articleCurrency;
	private String accCurrency;
	
	private long reserved;
	private long available;
	
	private String userStamp;
	private Timestamp timeStamp;
	private String sla;
	
	private int supportsAutomation;
	private List<String> availableForRoles;
}
