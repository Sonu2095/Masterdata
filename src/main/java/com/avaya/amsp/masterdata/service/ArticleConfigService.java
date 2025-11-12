package com.avaya.amsp.masterdata.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.ArticleConfig;
import com.avaya.amsp.masterdata.dtos.ArticleConfigDto;
import com.avaya.amsp.masterdata.repo.ArticleConfigRepository;
import com.avaya.amsp.masterdata.service.iface.ArticleConfigServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ArticleConfigService implements ArticleConfigServiceIface {

	@Autowired
	ArticleConfigRepository articleConfigRepo;

	@Autowired
	ModelMapper mapper;

	@Override
	public List<ArticleConfigDto> fetchMasterArticleAttribute() {
		log.info("fetching article config for master enabled");
		List<ArticleConfigDto> dtos = new ArrayList<ArticleConfigDto>();
		List<ArticleConfig> articles = articleConfigRepo.findAllMasterAttribute();
		if (articles != null && !articles.isEmpty()) {
			articles.forEach(article -> {
				ArticleConfigDto dto = mapper.map(article, ArticleConfigDto.class);
				dto.setId(article.getId());
				dto.setAttributeName(article.getAttributeName());
				dto.setApiAttributeName(article.getApiAttribute());
				dto.setRemark(article.getDescription());
				dtos.add(dto);
			});
		} else {
			log.info("No article config records found...");
		}
		return dtos;
	}

	@Override
	public List<ArticleConfigDto> fetchSlaveArticleAttribute() {
		log.info("fetching article config for slave enabled");
		List<ArticleConfigDto> dtos = new ArrayList<ArticleConfigDto>();
		List<ArticleConfig> articles = articleConfigRepo.findAllSlaveAttribute();
		if (articles != null && !articles.isEmpty()) {
			articles.forEach(article -> {
				ArticleConfigDto dto = mapper.map(article, ArticleConfigDto.class);
				dto.setId(article.getId());
				dto.setAttributeName(article.getAttributeName());
				dto.setApiAttributeName(article.getApiAttribute());
				dto.setRemark(article.getDescription());
				dtos.add(dto);
			});
		} else {
			log.info("No article config records found...");
		}
		return dtos;
	}
}
