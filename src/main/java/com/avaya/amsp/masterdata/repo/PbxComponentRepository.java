package com.avaya.amsp.masterdata.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.avaya.amsp.domain.PbxComponent;

public interface PbxComponentRepository extends JpaRepository<PbxComponent, Long> {
	
	List<PbxComponent> findByHwa(String hwa);
	List<PbxComponent> findByPbxSystem_id(Long idPbxSystem);
}
