package com.avaya.amsp.masterdata.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.PartListDto;
import com.avaya.amsp.masterdata.service.iface.PartListServiceIface;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/partLists")
public class PartListController {
	@Autowired
	private PartListServiceIface partListService;

	@GetMapping("/")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchPartLists() {
		log.info("requesting for getting all available partLists");
		List<PartListDto> response = new ArrayList<>();
		response = partListService.fetchAllPartList();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@PostMapping(value = "/")
	@PreAuthorize("hasAnyRole('BOSCH_ADMIN','AVAYA_ADMIN')")
	public ResponseEntity<Object> savePartList(@RequestBody PartListDto partListDto) {
		log.info("Saving partList : {}", partListDto);
		long id = partListService.savePartList(partListDto);
		return ResponseEntity.status(HttpStatus.OK).body(id);		
	}

}