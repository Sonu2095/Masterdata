package com.avaya.amsp.masterdata.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.PbxNumberRangeDto;
import com.avaya.amsp.masterdata.dtos.PbxSystemSiteDto;
import com.avaya.amsp.masterdata.dtos.PhoneNumberRangeDto;
import com.avaya.amsp.masterdata.service.PbxNumberRangeService;
import com.avaya.amsp.security.user.AMSPUser;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/pbxnumrange")
public class PbxPhoneNumberRangeController {

	@Autowired
	private PbxNumberRangeService pbxNumberRangeService;

	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchPbxNumberRange() {

		log.info("requesting for fetching pbx number range");
		List<PbxNumberRangeDto> response = new ArrayList<>();
		response = pbxNumberRangeService.fetchAllPbxNumberRange();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PostMapping("")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> persistPbxNumberRange(@Valid @RequestBody PbxNumberRangeDto request, @AuthenticationPrincipal AMSPUser amspUser) {

		log.info("request received to create a new pbx number range{}", request);
		request.setLogCreatedBy(amspUser.getUsername());
		String message = pbxNumberRangeService.createPbxNumberRange(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(message);

	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> updatePbxNumberRange(@RequestBody PbxNumberRangeDto request,
			@PathVariable("id") Long pbxId, @AuthenticationPrincipal AMSPUser amspUser) {
		request.setLogUpdatedBy(amspUser.getUsername());
		request.setId(pbxId);
		log.info("request received to update pbx number range {}", request);
		pbxNumberRangeService.updatePbxNumberRange(request);
		return ResponseEntity.status(HttpStatus.OK).body("");

	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deletePbxNumberRange(@PathVariable("id") Long pbxId) {

		log.info("request received to delete pbx number range {}", pbxId);
		pbxNumberRangeService.deletePbxNumberRange(pbxId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}
	
	@GetMapping("/pbxsystem/{pbxSystemId}")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchPbxNumberRangeByPbxSystem(@PathVariable("pbxSystemId") Long pbxSystemId) {

		log.info("requesting for fetching pbx number range by given pbx system : {}", pbxSystemId);
		
		List<PhoneNumberRangeDto> response = pbxNumberRangeService.fetchPbxNumberRangeByPbxSystem(pbxSystemId);
        if (!response.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            log.warn("PbxNumberRange not found for given pbxSystemId: {}", pbxSystemId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PbxNumberRange not found for given pbxSystemId: " +pbxSystemId);
        }
	}

}
