package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.BcsDto;

public interface BcsServiceIface {

	List<BcsDto> fetchAllBcsBunch();

}
