package com.avaya.amsp.masterdata.service.iface;

import java.util.List;
import java.util.Optional;

import com.avaya.amsp.domain.PbxSpecialPort;
import com.avaya.amsp.domain.PbxSystem;
import com.avaya.amsp.masterdata.dtos.AemPbxDto;
import com.avaya.amsp.masterdata.dtos.PbxComponentDto;
import com.avaya.amsp.masterdata.dtos.PbxPortDto;
import com.avaya.amsp.masterdata.dtos.PbxSpecialPortDto;
import com.avaya.amsp.masterdata.dtos.PbxSystemDto;
import com.avaya.amsp.masterdata.dtos.PbxSystemSiteDto;

public interface PbxSystemServiceIface {

	List<PbxSystemDto> fetchAllPbxSystems();
	Optional<PbxSystem> fetchPbxSystemByPhysicalPbx(String physicalPbx);

	void savePbxSystem(PbxSystemDto pbxSystemDto);

	void updatePbxSystem(PbxSystemDto pbxSystemDto);
	
	public void deletePbxSystem(Long pbxSystemId);
	
	AemPbxDto fetchAemPbxByPhysicalPbx(String physicalPbx);

	List<PbxComponentDto> fetchAllPbxComponentByPbxSystem(Long idPbxSystem);
	List<PbxSpecialPortDto> fetchAllPbxSpecialPortsByPbxSystem(Long idPbxSystem);
	List<PbxPortDto> fetchAllPbxPortsByPbxSystem(Long idPbxSystem);
	
	List<PbxSystemSiteDto> fetchAllPbxSystemBySiteId(Long siteId);

}
