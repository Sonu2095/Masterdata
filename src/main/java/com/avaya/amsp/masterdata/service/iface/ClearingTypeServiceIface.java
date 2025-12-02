package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.ClearingTypeDto;

public interface ClearingTypeServiceIface {

	List<ClearingTypeDto> fetchAllClearingType();

}
