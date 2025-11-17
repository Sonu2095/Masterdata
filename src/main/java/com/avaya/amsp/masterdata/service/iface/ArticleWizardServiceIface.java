package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.ArticleWizardDto;

public interface ArticleWizardServiceIface {
	
	List<ArticleWizardDto> fetchArticleWizard();

}
