package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.*;

public interface PbxClusterServiceIface {

	List<PbxClusterDto> fetchAllPbxClusters();

	long savePbxCluster(PbxClusterDto pbxClusterDto);

	PbxClusterDto updatePbxCluster(PbxClusterDto pbxClusterDto);

	List<PbxNumberLockDto> fetchAllNumberLockByPbxCluser(Long idPbxCluster);

	List<PbxSystemDto> fetchAllPbxSystemByPbxCluster(Long idPbxCluster);

	public void deletePbxClusters(Long clusterId);

	List<PbxNumberRangeDto> fetchNumberRangeByPbxCluster(int idPbxCluster);
	
	List<PhysicalPbxDto> fetchAllPhysicalPbxbyPbxCluster(Long idPbxCluster);

	PbxWrapper fetchPbxByAreaCode(String areaCode);
}
