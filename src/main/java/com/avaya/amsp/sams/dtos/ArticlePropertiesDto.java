package com.avaya.amsp.sams.dtos;


import com.avaya.amsp.domain.ArticleClearingTypeEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticlePropertiesDto {


    @NotNull
    private Long articleClusterId;

    private String name;
    private String remark;

    private Double pricePurchase_dollar;
    private Double pricePurchase_euro;
    private Double priceSales_dollar;
    private Double priceSales_euro;

    private Boolean obligatory;

    private Boolean alwaysInsert;

    private Boolean alwaysMove;

    private Boolean alwaysDelete;

    private Boolean alwaysChange;

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
    private Boolean automatedInstall;
}

