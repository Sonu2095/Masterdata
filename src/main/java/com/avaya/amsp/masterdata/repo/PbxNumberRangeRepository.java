package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avaya.amsp.domain.PbxNumberRange;

public interface PbxNumberRangeRepository extends JpaRepository<PbxNumberRange, Long> {

	List<PbxNumberRange> findByIdPbxCluster(int idPbxCluster);
	
	List<PbxNumberRange> findByIdPbxSystemAndActive(int idPbxSystem,int active);

}
