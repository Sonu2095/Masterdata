package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.ClusterDto;
import com.avaya.amsp.masterdata.dtos.CountryDto;
import com.avaya.amsp.masterdata.dtos.PartListDto;

public interface PartListServiceIface {

	List<PartListDto> fetchAllPartList();

	long savePartList(PartListDto partListDto);

}