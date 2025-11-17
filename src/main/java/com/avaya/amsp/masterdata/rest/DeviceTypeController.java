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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.DeviceTypeDto;
import com.avaya.amsp.masterdata.service.iface.DeviceTypeServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/deviceTypes")
@Slf4j
public class DeviceTypeController {
	@Autowired
	private DeviceTypeServiceIface deviceTypeService;

	@GetMapping(value = "",produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public Iterable<DeviceTypeDto> fetchDeviceTypes(){
		return deviceTypeService.getDeviceTypes();
	}

	@GetMapping(value = "clusters/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public List<DeviceTypeDto> fetchDeviceTypesForCluster(@PathVariable("id") Long clusterId){
		return deviceTypeService.getDeviceTypesByCluster(clusterId);
	}
	
	@PostMapping(value = "/search",produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public Iterable<DeviceTypeDto> searchDeviceTypes(@RequestBody DeviceTypeDto deviceTypeDto){
		log.info("Searching by : {}", deviceTypeDto);
		return deviceTypeService.searchDeviceTypeBy(deviceTypeDto);
	}
	
	@PostMapping(value="", produces=MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Boolean> persistDeviceType(@RequestBody DeviceTypeDto deviceTypeDto, @AuthenticationPrincipal AMSPUser amspUser){
		log.info("Saving deviceType dto {}", deviceTypeDto);
		deviceTypeDto.setLogCreatedBy(amspUser.getUsername());
		boolean saved = deviceTypeService.saveDeviceType(deviceTypeDto);
		ResponseEntity<Boolean> response = null;
		if(saved)
			response = new ResponseEntity<Boolean>(saved, HttpStatus.CREATED);
		else
			response = new ResponseEntity<Boolean>(saved, HttpStatus.INTERNAL_SERVER_ERROR);
		return response;
	}
	
	@PutMapping(value="", produces=MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Boolean> updateDeviceType(@RequestBody DeviceTypeDto deviceTypeDto, @AuthenticationPrincipal AMSPUser amspUser){
		log.info("Saving deviceType dto {}", deviceTypeDto);
		deviceTypeDto.setLogUpdatedBy(amspUser.getUsername());
		boolean saved = deviceTypeService.updateDeviceType(deviceTypeDto);
		ResponseEntity<Boolean> response = null;
		if(saved)
			response = new ResponseEntity<Boolean>(saved, HttpStatus.CREATED);
		else
			response = new ResponseEntity<Boolean>(saved, HttpStatus.INTERNAL_SERVER_ERROR);
		return response;
	}	
	@DeleteMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Boolean> deleteDeviceType(@PathVariable("id") long id){
		log.info("Deleting deviceType {}", id);
		deviceTypeService.deleteDeviceType(id);
		ResponseEntity<Boolean> response = new ResponseEntity<>(true, HttpStatus.OK);
		return response;
	}	
}
