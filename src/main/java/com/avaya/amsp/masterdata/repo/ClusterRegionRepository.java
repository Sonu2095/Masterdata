package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.ClusterRegion;
import com.avaya.amsp.domain.Region;
import com.avaya.amsp.masterdata.annotation.AuditLog;

public interface ClusterRegionRepository extends JpaRepository<ClusterRegion, Long> {
    boolean existsByClusterItemAndRegionNot(ClusterItem clusterItem, Region region);
    
    @AuditLog(action = "Insert",entity = "ClusterRegion",functionality = "Add Clusters to Region")
	default List<ClusterRegion> addClustersToRegion(List<ClusterRegion> listClusterRegion){
		return saveAll(listClusterRegion);
	}
    
    @Query(value = "SELECT cr FROM CLUSTER_REGION cr WHERE cr.region.id = :regionId AND cr.clusterItem.id in :clusterIdsList")
	List<ClusterRegion> findAllByRegionIdAndClusterIds(Long regionId, List<Long> clusterIdsList);

}
