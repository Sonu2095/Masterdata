package com.avaya.amsp.masterdata.service.iface;

import java.util.List;
import com.avaya.amsp.masterdata.dtos.PbxComponentDto;

public interface PbxComponentServiceIface {
		long savePbxComponent(PbxComponentDto pbxComponentDto);
		void updatePbxComponent(PbxComponentDto pbxComponentDto);
		void deletePbxComponent(long idPbxComponent);
		List<PbxComponentDto> fetchAllPbxComponent();
}