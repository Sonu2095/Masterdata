package com.avaya.amsp.masterdata.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.ArticleCategoryDto;
import com.avaya.amsp.masterdata.service.ArticleCategoryService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/articlecategory")
public class ArticleCategoryController {

	@Autowired
	ArticleCategoryService articleCategoryService;

	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchArticleCategory() {

		List<ArticleCategoryDto> response = new ArrayList<>();

		log.info("request received to fetch article category from database");
		response = articleCategoryService.fetchAllCategory();
		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

}
