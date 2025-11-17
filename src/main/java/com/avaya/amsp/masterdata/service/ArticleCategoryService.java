package com.avaya.amsp.masterdata.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.ArticleCategory;
import com.avaya.amsp.masterdata.dtos.ArticleCategoryDto;
import com.avaya.amsp.masterdata.repo.ArticleCategoryRepository;
import com.avaya.amsp.masterdata.service.iface.ArticleCategoryIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ArticleCategoryService implements ArticleCategoryIface {

	@Autowired
	ArticleCategoryRepository articleCategoryRepo;

	@Autowired
	ModelMapper mapper;

	@Override
	public List<ArticleCategoryDto> fetchAllCategory() {
		log.info("fetching article category from database");
		List<ArticleCategoryDto> dtos = new ArrayList<ArticleCategoryDto>();

		List<ArticleCategory> category = articleCategoryRepo.findAll();

		if (category != null && !category.isEmpty()) {

			category.forEach(artCategory -> {

				ArticleCategoryDto dto = mapper.map(artCategory, ArticleCategoryDto.class);
				dto.setName(artCategory.getName());
				dto.setId(artCategory.getId());
				dtos.add(dto);
			});

		} else {
			log.info("No article records found...");
		}
		return dtos;
	}

}
