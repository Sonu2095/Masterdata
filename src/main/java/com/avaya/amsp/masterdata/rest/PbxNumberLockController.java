package com.avaya.amsp.masterdata.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.PbxNumberLockDto;
import com.avaya.amsp.masterdata.dtos.PbxSpecialPortDto;
import com.avaya.amsp.masterdata.service.iface.PbxNumberLockServiceIface;
import com.avaya.amsp.masterdata.service.iface.PbxSpecialPortServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/pbxNumberLock")
public class PbxNumberLockController {

	@Autowired
	private PbxNumberLockServiceIface pbxNumberLockService;
	
	@PostMapping(value = "")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Object> savePbxNumberLock(@RequestBody PbxNumberLockDto pbxNumberLockDto, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("Saving pbxNumberLock : {}", pbxNumberLockDto);
		pbxNumberLockDto.setLogCreatedBy(amspUser.getUsername());
		String id = pbxNumberLockService.savePbxNumberLock(pbxNumberLockDto);
		return ResponseEntity.status(HttpStatus.OK).body(id);		
	}
	
	@PutMapping(value = "")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Object> updatePbxNumberLock(@RequestBody PbxNumberLockDto pbxNumberLockDto, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("Updating pbxNumberLock : {}", pbxNumberLockDto);
		pbxNumberLockDto.setLogUpdatedBy(amspUser.getUsername());
		pbxNumberLockService.updatePbxNumberLock(pbxNumberLockDto);
		return ResponseEntity.status(HttpStatus.OK).body(true);		
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public boolean deletePbxNumberLock(@PathVariable("id") Long idPbxNumberLock) {
		log.info("Deleted pbxNumberLock %s ", idPbxNumberLock);
		boolean deleted = pbxNumberLockService.deletePbxNumberLock(idPbxNumberLock);
		return deleted;
	}

}
