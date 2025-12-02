package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.ArticleConfigDto;

public interface ArticleConfigServiceIface {

	List<ArticleConfigDto> fetchMasterArticleAttribute();

	List<ArticleConfigDto> fetchSlaveArticleAttribute();

}
