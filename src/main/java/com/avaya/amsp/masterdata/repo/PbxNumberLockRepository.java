package com.avaya.amsp.masterdata.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avaya.amsp.domain.PbxNumberLock;
import com.avaya.amsp.masterdata.dtos.PbxNumberLockDto;

public interface PbxNumberLockRepository extends JpaRepository<PbxNumberLock, Long> {

	List<PbxNumberLock> findBypbxCluster_Id(Long idPbxCluster);
	List<PbxNumberLock> findByclusterItem_Id(Long idCluster);

}
