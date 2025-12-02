package com.avaya.amsp.masterdata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.ArticleClusterI18NName;
import com.avaya.amsp.masterdata.repo.ArticleClusterI18NRepository;

@Service
public class ArticleClusterI18NNameService {
	
	@Autowired
	ArticleClusterI18NRepository articleClusterI18NRepository;

	@Cacheable(value = "articleTranslations", key = "#articleId + ':' + #langId")
	public String getTranslation(Long articleId, String langId, String defaultName) {
		ArticleClusterI18NName translation = articleClusterI18NRepository.findByArticleClusterIdAndLaguage(articleId,
				langId);
		return (translation != null) ? translation.getTranslation() : defaultName;
	}
}
