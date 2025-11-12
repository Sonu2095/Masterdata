package com.avaya.amsp.masterdata.dtos;


import java.time.LocalDateTime;

import com.avaya.amsp.domain.PartListPropertyType;
import com.avaya.amsp.domain.PartListValueTransfer;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
//@ToString(includeFieldNames = true)
public class PartListDto {

	private long id;
	private int isMasterList;
	private PartListValueTransfer withValueTransfer;
	private int subArticleCount;
	private String monthlyCost;
	private String onetimeCost;
	private PartListPropertyType propertyType;
	private String logCreatedBy;
	private LocalDateTime logCreatedOn;
	private String logUpdatedBy;
	private LocalDateTime logUpdatedOn;
	private ClusterDto cluster;
	private ArticleDto article;
//	private Set<PartListArticle> partListArticle;

}
