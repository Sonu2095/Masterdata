package com.avaya.amsp.masterdata.service.iface;

import java.util.List;
import java.util.Optional;

import com.avaya.amsp.domain.ServiceCode;
import com.avaya.amsp.masterdata.dtos.ServiceCodeDto;

public interface ServiceCodeServiceIface {

	List<ServiceCodeDto> fetchAvailableServiceCode();

	String persistService(ServiceCodeDto request, String userName);

	Optional<ServiceCode> fechServiceCode(String serviceCode);

	void updateServiceCode(ServiceCodeDto request, String userName);

	void deleteServiceCode(String serviceCode);

}
