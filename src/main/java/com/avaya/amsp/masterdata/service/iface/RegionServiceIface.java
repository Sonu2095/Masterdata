package com.avaya.amsp.masterdata.service.iface;

import java.util.List;
import java.util.Optional;

import com.avaya.amsp.domain.Region;
import com.avaya.amsp.masterdata.dtos.ClusterDto;
import com.avaya.amsp.masterdata.dtos.RegionDto;
import com.avaya.amsp.masterdata.dtos.RegionToClusterDto;

	public interface RegionServiceIface {

	List<RegionDto> fetchAllRegions();

	public void createRegion(RegionDto regionDto);

	public void updateRegion(RegionDto regionDto);

	public void deleteRegion(Long regionId);

	public Optional<Region> fetchRegionByName(String name);

	public List<ClusterDto> fetchClustersByRegion(Long regionId);
	
	public List<ClusterDto> fetchClustersByRegionAndClusterIds(Long regionId, List<Long> clusterIds);

	public void addClustersToAllocation(Long regionId, RegionToClusterDto clusters);

	public void deleteClustersFromAllocation(Long regionId, RegionToClusterDto clusters);

}
