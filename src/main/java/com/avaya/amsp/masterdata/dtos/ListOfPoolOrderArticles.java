package com.avaya.amsp.masterdata.dtos;

import java.util.List;

import lombok.Data;

@Data
public class ListOfPoolOrderArticles {
	private List<PoolOrderArticleDTO> listOfArticles;
}
