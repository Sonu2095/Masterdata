package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.ShippingAddressDto;

public interface ShippingAddressServiceIface {

	boolean saveAddress(ShippingAddressDto address);

	List<ShippingAddressDto> getAddresses();

	boolean deleteAddress(long id);

	List<ShippingAddressDto> searchAddressesBy(ShippingAddressDto addressDto);

	ShippingAddressDto getAddress(Long id);

	boolean updateAddress(ShippingAddressDto addressDto);

}