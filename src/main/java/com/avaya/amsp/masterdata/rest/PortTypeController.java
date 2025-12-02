package com.avaya.amsp.masterdata.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.PortTypeDto;
import com.avaya.amsp.masterdata.service.PortTypeService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/port-types")
public class PortTypeController {

	@Autowired
	PortTypeService portTypeService;

	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchPortTypes() {

		List<PortTypeDto> response = new ArrayList<>();

		log.info("request received to fetch portytypes from database");
		response = portTypeService.fetchAllPortTypes();
		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	@PostMapping("")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> persistPortTypes(@Valid @RequestBody PortTypeDto request) {

		log.info("request received to persist portytypes to database {}", request);
		portTypeService.persistPortType(request);
		return ResponseEntity.status(HttpStatus.CREATED).body("");

	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> updatePortTypes(@RequestBody PortTypeDto request, @PathVariable("id") Long portId) {

		request.setId(portId);
		log.info("request received to update portytypes to database {}", request);
		portTypeService.updatePortType(request);
		return ResponseEntity.status(HttpStatus.OK).body("");

	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deletePortTypes(@PathVariable("id") Long portId) {

		log.info("request received to delete portytypes from DB with Id as {}", portId);
		portTypeService.removePortType(portId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}

}
