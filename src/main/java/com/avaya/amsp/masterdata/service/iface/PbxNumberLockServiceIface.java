package com.avaya.amsp.masterdata.service.iface;

import com.avaya.amsp.masterdata.dtos.PbxNumberLockDto;
import com.avaya.amsp.masterdata.dtos.PbxSystemDto;

public interface PbxNumberLockServiceIface {

	long savePbxNumberLock(PbxNumberLockDto pbxNumberLockDto);

	void updatePbxNumberLock(PbxNumberLockDto pbxNumberLockDto);
	
	boolean deletePbxNumberLock(long idPbxNumberLock);
}
