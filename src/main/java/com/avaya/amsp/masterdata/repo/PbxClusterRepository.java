package com.avaya.amsp.masterdata.repo;

import java.util.List;
import java.util.Optional;

import com.avaya.amsp.domain.PbxSystem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avaya.amsp.domain.PbxCluster;

public interface PbxClusterRepository extends JpaRepository<PbxCluster, Long> {

	@Query(value = "select C from pbx_cluster  C where  C.clusterItem.id= :id and C.active=1")
	public List<PbxCluster> findByClusterId(Long id);
	
	@Query(value = "select C from pbx_cluster  C where  C.clusterItem.id= :id and C.active=1")
	public Page<PbxCluster> findByClusterId(Long id, Pageable pageable);

    List<PbxCluster> findByClusterItemId(Long clusterItemId);

	Optional<PbxCluster> findFirstByAreacodeAndActiveTrue(String areacode);
	
	@Query(value="select name from pbx_cluster where id= :pbxClusterId and active=1")
	public String getClusterName(int pbxClusterId);
}
