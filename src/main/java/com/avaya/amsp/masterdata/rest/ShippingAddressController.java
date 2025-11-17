package com.avaya.amsp.masterdata.rest;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.ShippingAddressDto;
import com.avaya.amsp.masterdata.service.iface.ShippingAddressServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/shippingaddresses")
@Slf4j
public class ShippingAddressController {
	@Autowired
	private ShippingAddressServiceIface shippingService;

	@GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public Iterable<ShippingAddressDto> fetchAddresses() {
		return shippingService.getAddresses();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ShippingAddressDto fetchAddress(@PathVariable("id") Long id) {
		return shippingService.getAddress(id);
	}

	@PostMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('BOSCH_ADMIN','AVAYA_ADMIN')")
	public Iterable<ShippingAddressDto> searchAddresses(@RequestBody ShippingAddressDto addressDto) {
		log.info("Searching by : {}", addressDto);
		return shippingService.searchAddressesBy(addressDto);
	}

	@PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Boolean> persistAddress(@RequestBody ShippingAddressDto addressDto) {
		log.info("Saving address dto {}", addressDto);
		boolean saved = shippingService.saveAddress(addressDto);
		ResponseEntity<Boolean> response = null;
		if (saved)
			response = new ResponseEntity<Boolean>(saved, HttpStatus.CREATED);
		else
			response = new ResponseEntity<Boolean>(saved, HttpStatus.INTERNAL_SERVER_ERROR);
		return response;
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Boolean> deleteAddress(@PathVariable("id") Long id) {
		log.info("Deleting address {}", id);
		shippingService.deleteAddress(id);
		ResponseEntity<Boolean> response = new ResponseEntity<>(true, HttpStatus.OK);
		return response;
	}

	@PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Boolean> updateAddress(@RequestBody ShippingAddressDto addressDto, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("Updating shipping address with id {}", addressDto.getId());
		addressDto.setLogUpdatedBy(amspUser.getUsername());
		boolean updated = shippingService.updateAddress(addressDto);
		return updated ? new ResponseEntity<>(true, HttpStatus.OK) : new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
	}
}
