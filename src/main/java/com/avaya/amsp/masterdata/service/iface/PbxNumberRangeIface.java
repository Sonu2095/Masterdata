package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.PbxNumberRangeDto;
import com.avaya.amsp.masterdata.dtos.PhoneNumberRangeDto;

public interface PbxNumberRangeIface {

	List<PbxNumberRangeDto> fetchAllPbxNumberRange();

	public void createPbxNumberRange(PbxNumberRangeDto pbxNumberRangeDto);

	public void updatePbxNumberRange(PbxNumberRangeDto pbxNumberRangeDto);

	public void deletePbxNumberRange(Long pbxId);
	
	public List<PhoneNumberRangeDto> fetchPbxNumberRangeByPbxSystem(Long id);

}
