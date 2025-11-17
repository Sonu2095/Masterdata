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
import com.avaya.amsp.masterdata.dtos.PbxSpecialPortDto;
import com.avaya.amsp.masterdata.service.iface.PbxSpecialPortServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/pbxSpecialPorts")
public class PbxSpecialPortController {
	@Autowired
	private PbxSpecialPortServiceIface pbxSpecialPortService;

	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchPbxSpecialPorts() {
		log.info("requesting for getting all pbxSpecialPorts");
		List<PbxSpecialPortDto> response = new ArrayList<>();
		response = pbxSpecialPortService.fetchAllPbxSpecialPorts();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@PostMapping(value = "")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Object> savePbxSpecialPort(@RequestBody PbxSpecialPortDto pbxSpecialPortDto, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("Saving pbxSpecialPortDto : {}", pbxSpecialPortDto);
		pbxSpecialPortDto.setLogCreatedBy(amspUser.getUsername());
		long id = pbxSpecialPortService.savePbxSpecialPort(pbxSpecialPortDto);
		return ResponseEntity.status(HttpStatus.OK).body(id);		
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> updatePbxSpecialPort(@RequestBody PbxSpecialPortDto pbxSpecialPortDto,
			@PathVariable("id") Long pbxSpecialPortId, @AuthenticationPrincipal AMSPUser amspUser) {
		pbxSpecialPortDto.setId(pbxSpecialPortId);
		pbxSpecialPortDto.setLogUpdatedBy(amspUser.getUsername());
		log.info("request received to update pbx special port with Id {}", pbxSpecialPortDto);
		pbxSpecialPortService.updatePbxSpecialPort(pbxSpecialPortDto);
		return ResponseEntity.status(HttpStatus.OK).body("");

	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deletePbxSpecialPort(@PathVariable("id") Long pbxSpecialPortId) {
		log.info("request received to delete pbx special port with Id {}", pbxSpecialPortId);
		pbxSpecialPortService.deletePbxSpecialPort(pbxSpecialPortId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}
}