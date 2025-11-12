package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.PortStatusDto;

public interface PortStatusServiceIface {

	List<PortStatusDto> fetchAllPortStatus();

}
