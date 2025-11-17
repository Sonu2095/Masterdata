package com.avaya.amsp.masterdata.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.ShippingAddressTypeDto;
import com.avaya.amsp.masterdata.service.iface.ShippingAddressTypeServiceIface;

import lombok.extern.slf4j.Slf4j;

@RestController()
@Slf4j
@RequestMapping("/v1/shipping-addresses-types")
public class ShippingAddressTypeController {

	@Autowired
	private ShippingAddressTypeServiceIface shippingAddressTypeService;
	
	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public List<ShippingAddressTypeDto> getServiceTypes() {
		log.info("Fetching all shippingAddress types");
		return shippingAddressTypeService.getAllShippingTypes();
	}

}
