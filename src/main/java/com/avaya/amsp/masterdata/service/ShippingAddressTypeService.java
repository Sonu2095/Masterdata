package com.avaya.amsp.masterdata.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;

import com.avaya.amsp.domain.ShippingType;
import com.avaya.amsp.masterdata.dtos.ShippingAddressTypeDto;
import com.avaya.amsp.masterdata.repo.ShippingAddressTypeRepository;
import com.avaya.amsp.masterdata.service.iface.ShippingAddressTypeServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ShippingAddressTypeService implements ShippingAddressTypeServiceIface {

	@Autowired
	public ShippingAddressTypeRepository  shippingAddressTypeRepo;
	@Autowired
	private ModelMapper mapper;
	
	@Override
	public List<ShippingAddressTypeDto> getAllShippingTypes() {
		
		List<ShippingType> shippingTypes = shippingAddressTypeRepo.findAll();
		log.info("Got shippingAddressType as {}", shippingTypes);
		List<ShippingAddressTypeDto> shippingAddressTypeDtos = new ArrayList<>();
		for(ShippingType shippingType : shippingTypes)
			shippingAddressTypeDtos.add(mapper.map(shippingType, ShippingAddressTypeDto.class));
		return shippingAddressTypeDtos;

	}

}
