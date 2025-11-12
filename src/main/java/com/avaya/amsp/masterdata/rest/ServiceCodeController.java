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

import com.avaya.amsp.masterdata.dtos.ServiceCodeDto;
import com.avaya.amsp.masterdata.service.ServiceCodeService;
import com.avaya.amsp.masterdata.service.iface.ServiceCodeServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/servicecode")
public class ServiceCodeController {
	@Autowired
	ServiceCodeServiceIface servicecodeService;

	/**
	 * get all available ServiceCode for an Article
	 * 
	 * @return
	 */
	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchServiceCodeValues() {
		List<ServiceCodeDto> response = new ArrayList<>();
		log.info("request received to fetch available servicecode values");
		response = servicecodeService.fetchAvailableServiceCode();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PostMapping("")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> persistServiceCode(@Valid @RequestBody ServiceCodeDto request, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to persist servicecode to database {}", request);
		//servicecodeService.persistService(request, amspUser.getUsername());
		//return ResponseEntity.status(HttpStatus.CREATED).body("created request");
		String status = servicecodeService.persistService(request, amspUser.getUsername());
		return ResponseEntity.status(HttpStatus.CREATED).body(status);

	}

	/**
	 * This method is to update the existing servicecode
	 * 
	 * @param request
	 * @param ServiceCode
	 * @return 200 OK
	 */

	@PutMapping("/{servicecode}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> updateServiceCode(@RequestBody ServiceCodeDto request,
			@PathVariable("servicecode") String serviceCode, @AuthenticationPrincipal AMSPUser amspUser) {

		request.setServiceCode(serviceCode);
		log.info("request received to update servicecode {}", request);
		servicecodeService.updateServiceCode(request, amspUser.getUsername());
		return ResponseEntity.status(HttpStatus.OK).body("updated");

	}

	/**
	 * This method will delete a serviceCode
	 * 
	 * @param ServiceCode
	 * @return 200 ok
	 */

	@DeleteMapping("/{servicecode}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deleteServiceCode(@PathVariable("servicecode") String serviceCode) {
		log.info("request received to delete servicecode {}", serviceCode);
		servicecodeService.deleteServiceCode(serviceCode);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}

}
