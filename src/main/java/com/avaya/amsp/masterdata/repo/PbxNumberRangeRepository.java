package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.avaya.amsp.domain.PbxNumberRange;

public interface PbxNumberRangeRepository extends JpaRepository<PbxNumberRange, Long> {

	List<PbxNumberRange> findByIdPbxCluster(int idPbxCluster);
	
	List<PbxNumberRange> findByIdPbxSystemAndActive(int idPbxSystem,int active);

	@Query(value = "select id from pbx_number_range where id= :pbxNumberRange", nativeQuery = true)
	Long fetchPbxNumberRange(@Param("pbxNumberRange") Long pbxNumberRange);

}
