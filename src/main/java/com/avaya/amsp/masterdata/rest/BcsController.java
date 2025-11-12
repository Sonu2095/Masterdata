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

import com.avaya.amsp.masterdata.dtos.BcsDto;
import com.avaya.amsp.masterdata.service.BcsService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/bcsbunch")

/**
 * 
 * @author yadav188 This is working as controller for connection BCS
 *
 */
public class BcsController {

	@Autowired
	BcsService bcsService;

	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchBcsBunch() {

		log.info("requesting for getting all available bcsbunch");
		List<BcsDto> response = new ArrayList<>();
		response = bcsService.fetchAllBcsBunch();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
