package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.ShippingAddressTypeDto;

public interface ShippingAddressTypeServiceIface {
	List<ShippingAddressTypeDto> getAllShippingTypes();

}
