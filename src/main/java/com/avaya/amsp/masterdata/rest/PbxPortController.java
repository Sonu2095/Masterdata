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

import com.avaya.amsp.masterdata.dtos.PbxPortDto;
import com.avaya.amsp.masterdata.service.iface.PbxPortServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/pbxPorts")
public class PbxPortController {
	@Autowired
	private PbxPortServiceIface pbxPortService;

	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchPbxPorts() {
		log.info("requesting for getting all pbxPorts");
		List<PbxPortDto> response = new ArrayList<>();
		response = pbxPortService.fetchAllPbxPorts();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@PostMapping(value = "")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Object> savePbxPort(@RequestBody PbxPortDto pbxPortDto, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("Saving pbxPortDto : {}", pbxPortDto);
		pbxPortDto.setLogCreatedBy(amspUser.getUsername());
		long id = pbxPortService.savePbxPort(pbxPortDto);
		return ResponseEntity.status(HttpStatus.OK).body(id);		
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> updatePbxPort(@RequestBody PbxPortDto pbxPortDto,
			@PathVariable("id") Long pbxPortId, @AuthenticationPrincipal AMSPUser amspUser) {
		pbxPortDto.setId(pbxPortId);
		pbxPortDto.setLogUpdatedBy(amspUser.getUsername());
		log.info("request received to update pbx port with Id {}", pbxPortDto);
		pbxPortService.updatePbxPort(pbxPortDto);
		return ResponseEntity.status(HttpStatus.OK).body("");

	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deletePbxPort(@PathVariable("id") Long pbxPortId) {
		log.info("request received to delete pbx port with Id {}", pbxPortId);
		pbxPortService.deletePbxPort(pbxPortId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}
}