package com.avaya.amsp.masterdata.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.ServiceTypeDto;
import com.avaya.amsp.masterdata.service.iface.ServiceTypeServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import lombok.extern.slf4j.Slf4j;

@RestController()
@Slf4j
@RequestMapping("v1/service-types")
public class ServiceTypeController {

	@Autowired
	private ServiceTypeServiceIface serviceTypeService;
	
	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public List<ServiceTypeDto> getServiceTypes() {
		log.info("Fetching all serviceTypes");
		return serviceTypeService.getAllServiceTypes();
	}

	@PostMapping(value="", produces=MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Boolean> persistServiceType(@RequestBody ServiceTypeDto serviceTypeDto, @AuthenticationPrincipal AMSPUser amspUser){
		log.info("Saving serviceType dto {}", serviceTypeDto);
		serviceTypeDto.setLogCreatedBy(amspUser.getUsername());
		serviceTypeDto.setLogUpdatedBy(amspUser.getUsername());
		boolean saved = serviceTypeService.saveServiceType(serviceTypeDto);
		ResponseEntity<Boolean> response = null;
		if(saved)
			response = new ResponseEntity<Boolean>(saved, HttpStatus.CREATED);
		else
			response = new ResponseEntity<Boolean>(saved, HttpStatus.INTERNAL_SERVER_ERROR);
		return response;
	}
	
	@DeleteMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Boolean> deleteServiceType(@PathVariable("id") long id){
		log.info("Deleting serviceType with {}", id);
		boolean deleted = serviceTypeService.deleteServiceType(id);
		ResponseEntity<Boolean> response = new ResponseEntity<>(deleted, HttpStatus.OK);
		return response;
	}
}
