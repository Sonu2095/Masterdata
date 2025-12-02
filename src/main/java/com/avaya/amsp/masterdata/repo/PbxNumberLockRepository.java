package com.avaya.amsp.masterdata.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.avaya.amsp.domain.PbxNumberLock;
import com.avaya.amsp.masterdata.dtos.PbxNumberLockDto;

public interface PbxNumberLockRepository extends JpaRepository<PbxNumberLock, Long> {

	List<PbxNumberLock> findBypbxCluster_Id(Long idPbxCluster);
	List<PbxNumberLock> findByclusterItem_Id(Long idCluster);
	
	@Query(value ="select id from pbx_number_lock where id= :pbxNumber", nativeQuery = true)
	Long getPbxNumber(@Param("pbxNumber") Long pbxNumber);

}
