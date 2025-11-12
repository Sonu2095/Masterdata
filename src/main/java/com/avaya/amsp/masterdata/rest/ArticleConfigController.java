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

import com.avaya.amsp.masterdata.dtos.ArticleConfigDto;
import com.avaya.amsp.masterdata.service.ArticleConfigService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/article-attributes")
public class ArticleConfigController {

	@Autowired
	ArticleConfigService articleConfigService;

	@GetMapping("/mastermerge")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchArticleConfigMaster() {

		List<ArticleConfigDto> response = new ArrayList<>();
		log.info("request received to fetch article config for master mergeslave");
		response = articleConfigService.fetchMasterArticleAttribute();
		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	@GetMapping("/slavemerge")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchArticleConfigSlave() {

		List<ArticleConfigDto> response = new ArrayList<>();
		log.info("request received to fetch article config for master merge");
		response = articleConfigService.fetchSlaveArticleAttribute();
		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

}
