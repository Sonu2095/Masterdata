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

import com.avaya.amsp.masterdata.dtos.PbxComponentDto;
import com.avaya.amsp.masterdata.service.iface.PbxComponentServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/pbxComponent")
public class PbxComponentController {

	@Autowired
	private PbxComponentServiceIface pbxComponentService;
	
	@GetMapping(value = "")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchPbxComponents() {
		log.info("requesting for getting all pbxComponent");
		List<PbxComponentDto> response = new ArrayList<>();
		response = pbxComponentService.fetchAllPbxComponent();
		return ResponseEntity.status(HttpStatus.OK).body(response);	
	}
	
	@PostMapping(value = "")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Object> save(@RequestBody PbxComponentDto pbxComponentDto, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("Saving pbxComponent : {}", pbxComponentDto);
		pbxComponentDto.setLogCreatedBy(amspUser.getUsername());
		long id = pbxComponentService.savePbxComponent(pbxComponentDto);
		return ResponseEntity.status(HttpStatus.OK).body(id);		
	}
	
	@PutMapping(value = "")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Object> updatePbxComponent(@RequestBody PbxComponentDto pbxComponentDto, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("Updating pbxComponent : {}", pbxComponentDto);
		pbxComponentDto.setLogUpdatedBy(amspUser.getUsername());
		pbxComponentService.updatePbxComponent(pbxComponentDto);
		return ResponseEntity.status(HttpStatus.OK).body(true);		
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Object> deletePbxComponent(@PathVariable("id") Long idPbxComponent) {
		log.info("Deleted pbxComponent %s ", idPbxComponent);
		pbxComponentService.deletePbxComponent(idPbxComponent);
		return ResponseEntity.status(HttpStatus.OK).body("");		
	}

}
