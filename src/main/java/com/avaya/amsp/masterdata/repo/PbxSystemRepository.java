package com.avaya.amsp.masterdata.repo;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.avaya.amsp.domain.PbxSystem;

public interface PbxSystemRepository extends JpaRepository<PbxSystem, Long> {
	
	public PbxSystem findByPhysicalPbx(String physicalPbx);

	List<PbxSystem> findByPbxCluster_Id(Long idPbxCluster); 
	List<PbxSystem> findByPbxCluster_ClusterItem_Id(Long idCluster);

    @Query("SELECT p.physicalPbx FROM pbx_system p WHERE p.pbxCluster.id = :idPbxCluster")
	List<String> findPhysicalPbxByPbxCluster_Id(@Param("idPbxCluster") Long idPbxCluster);
    
    @Query("SELECT p.physicalPbx FROM pbx_system p WHERE p.id = :id")
    String findPhysicalPbxById(Integer id);
    
    @Query("SELECT p FROM pbx_system p WHERE p.pbxCluster.id IN(select c.id from pbx_cluster c WHERE c.clusterItem.id = :idCluster )")
    List<PbxSystem> findByCluster_Id(Long idCluster);
    
    @Query("SELECT p FROM pbx_system p WHERE p.pbxCluster.id IN(select c.id from pbx_cluster c WHERE c.clusterItem.id = :idCluster )")
    Page<PbxSystem> findByCluster_Id(Long idCluster, Pageable pageable);

    @Query("SELECT p FROM pbx_system p WHERE p.pbxCluster.id = :pbx_cluster_id and p.active=1")
    List<PbxSystem> fetchByPbxClusterId(long pbx_cluster_id);

    @Query(value="""
             select s.id,s.name  from pbx_cluster pc join pbx_system ps on pc.id = ps.id_pbx_cluster
              join pbxsystem_site ps2 on ps2.id_pbxsystem = ps.id
              join site s on s.id = ps2.id_site
              where pc.active=1 and pc.areacode = :areaCode and ps.is_sfbs_system = 1 and ps2.active=1 and s.active=1
             """,nativeQuery = true)
    List< Object[] > fetchPbxSystemSites(String areaCode);
    
    @Query(value = "SELECT p.physicalPbx FROM pbx_system p WHERE p.id = :id")
    String findNameById(Long id);
}
