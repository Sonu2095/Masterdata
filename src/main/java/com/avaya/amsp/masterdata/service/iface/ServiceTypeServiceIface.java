package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.DeviceTypeDto;
import com.avaya.amsp.masterdata.dtos.ServiceTypeDto;

public interface ServiceTypeServiceIface {
	List<ServiceTypeDto> getAllServiceTypes();

	boolean saveServiceType(ServiceTypeDto serviceTypeDto);

	boolean deleteServiceType(long id);
}	
