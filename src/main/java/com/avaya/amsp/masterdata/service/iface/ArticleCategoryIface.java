package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.ArticleCategoryDto;
import com.avaya.amsp.masterdata.dtos.ArticleDto;

public interface ArticleCategoryIface {
	
	List<ArticleCategoryDto> fetchAllCategory();

}
