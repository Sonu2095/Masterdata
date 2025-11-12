package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.avaya.amsp.domain.PbxPort;

@Repository
public interface PbxPortRepository extends JpaRepository<PbxPort, Long> {

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM pbx_port p " +
            "WHERE p.pbxSystem.id = :idPbxSystem AND p.cluster.id = :idCluster AND p.portType.id = :idPortType AND p.aemPortType.id = :idPortTypeAem")
     boolean existsByIdPbxSystemAndIdClusterAndIdPortTypeAndIdPortTypeAem(@Param("idPbxSystem") Integer idPbxSystem,
                                                                          @Param("idCluster") Integer idCluster,
                                                                          @Param("idPortType") Integer idPortType,
                                                                          @Param("idPortTypeAem") Integer idPortTypeAem);

	List<PbxPort> findByPbxSystem_id(Long idPbxSystem);

}
