package com.avaya.amsp.masterdata.dtos;

import lombok.Data;

@Data
public class ArticlePartListDto {

	private Integer subArticleCount;
	private double monthlySalesPrice;
	private double oneTimeSalesPrice;
	private double monthlyPurchasePrice;
	private double oneTimePurchasePrice;
	private ArticleDto articles;

}
