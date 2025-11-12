package com.avaya.amsp.masterdata.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.security.user.AMSPUser;
import com.avaya.amsp.shared.dtos.AMSPTimeZoneDTO;
import com.avaya.amsp.shared.util.AMSPUtils;
import com.avaya.amsp.util.timezones.AMSPTimeZonesService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class TimezoneController {

	@Autowired
	private AMSPTimeZonesService timezoneService;

	@GetMapping("/v1/timezones")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<?> fetchTimezones(@AuthenticationPrincipal AMSPUser amspUser) {

		log.info("requesting for getting all available timezones");
		List<AMSPTimeZoneDTO> response = new ArrayList<>();
		response = timezoneService.getTimeZones(AMSPUtils.getLocale(amspUser.getDefaultLanguageId()));
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}