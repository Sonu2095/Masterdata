package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;

import com.avaya.amsp.domain.ArticleClearingTypeEnum;
import com.avaya.amsp.domain.PartsPropertyEnum;

import lombok.Data;

@Data
public class ArticleToPoolDto {

	private Long id;
	// private Long articleId;
	// private Long clusterId;
	private String name;
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
	private int lifeTime;
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
	private int isPart;
	private PartsPropertyEnum Property;
	private Boolean valueTransfer;
	private Boolean isMasterPartStatus;
	
	private long reserved;
	private long available;

}
