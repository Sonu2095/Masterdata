package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.PbxPortDto;

public interface PbxPortServiceIface {

	List<PbxPortDto> fetchAllPbxPorts();
	long savePbxPort(PbxPortDto pbxPortDto);
	void updatePbxPort(PbxPortDto pbxPortDto);
	void deletePbxPort(Long pbxPortId);
}
