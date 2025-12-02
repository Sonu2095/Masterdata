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

import com.avaya.amsp.masterdata.dtos.PhoneNumberTypeDto;
import com.avaya.amsp.masterdata.service.PhoneNumberTypeService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/phonenumbertype")
public class PhoneNumberTypeController {

	@Autowired
	PhoneNumberTypeService phoneNumberTypeService;

	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchPhoneNumberType() {

		log.info("requesting for fetching available phone number type");
		List<PhoneNumberTypeDto> response = new ArrayList<>();
		response = phoneNumberTypeService.fetchPhoneNumberType();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
