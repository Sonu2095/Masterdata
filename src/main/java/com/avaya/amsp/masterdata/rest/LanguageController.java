package com.avaya.amsp.masterdata.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.LanguageDto;
import com.avaya.amsp.masterdata.service.LanguageService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j

/**
 * 
 * @author yadav188
 * This is working as controller for Language
 *
 */
public class LanguageController {

	@Autowired
	private LanguageService languageService;

	@GetMapping("/v1/languages")
	//@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchLanguages() {

		log.info("requesting for getting all available languages");
		List<LanguageDto> response = new ArrayList<>();
		response = languageService.fetchAllLanguages();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
