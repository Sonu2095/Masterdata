package com.avaya.amsp.masterdata.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.CountryDto;
import com.avaya.amsp.masterdata.service.CountryService;
import com.avaya.amsp.masterdata.service.iface.CountryServiceIface;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j

public class CountryController {
	@Autowired
	private CountryService countryService;

	@GetMapping("/v1/countries")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchCountries() {

		log.info("requesting for getting all available countries");
		List<CountryDto> response = new ArrayList<>();
		response = countryService.fetchAllCountries();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
