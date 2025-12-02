package com.avaya.amsp.masterdata.dtos;

import com.avaya.amsp.domain.ArticleClearingTypeEnum;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticlePropertiesDto {
	
	

	@NotNull
	private Long articleClusterId; // represent master article id

	private String name;
	private String nameEnglish;
	private String nameGerman;
	private String remark;

	private Double pricePurchase_dollar;
	private Double pricePurchase_euro;
	private Double priceSales_dollar;
	private Double priceSales_euro;

	@Min(value = 0, message = "pseudoFlag should have value either 0 or 1")
	@Max(value = 1, message = "pseudoFlag should have value either 0 or 1")
	private Boolean obligatory;

	@Min(value = 0, message = "pseudoFlag should have value either 0 or 1")
	@Max(value = 1, message = "pseudoFlag should have value either 0 or 1")
	private Boolean alwaysInsert;

	@Min(value = 0, message = "pseudoFlag should have value either 0 or 1")
	@Max(value = 1, message = "pseudoFlag should have value either 0 or 1")
	private Boolean alwaysMove;

	@Min(value = 0, message = "pseudoFlag should have value either 0 or 1")
	@Max(value = 1, message = "pseudoFlag should have value either 0 or 1")
	private Boolean alwaysDelete;

	@Min(value = 0, message = "pseudoFlag should have value either 0 or 1")
	@Max(value = 1, message = "pseudoFlag should have value either 0 or 1")
	private Boolean subscriberSwap;
	
	private ArticleClearingTypeEnum articleClearingType;
	private Long quantity;
	private Long bcsId;
	private Boolean single;
	private String additionalInfo; //value_default
	private Boolean readOnly;
	private String articleWizardType;
	private Double puPrice;
	private Double saPrice;
	private String currency;
	private PropertyDto partProperties;
	
	private Long available;
	private Long reserved;
	
	private Long poolId;
	private String poolKey;
	private String clusterKey;
	private String siteKey;
	
	private Boolean shippingReq;
    private Boolean assemblyReq;
    
}

