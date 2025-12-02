package com.avaya.amsp.masterdata.service.iface;

import java.util.List;
import java.util.Optional;

import com.avaya.amsp.domain.PortType;
import com.avaya.amsp.masterdata.dtos.PortTypeDto;

public interface PortTypeServiceIface {
	
	List<PortTypeDto> fetchAllPortTypes();

	public void persistPortType(PortTypeDto dto);

	public void updatePortType(PortTypeDto dto);

	public void removePortType(Long portId);
	
	public Optional<PortType> fetchPortTypeByType(String type);

}
