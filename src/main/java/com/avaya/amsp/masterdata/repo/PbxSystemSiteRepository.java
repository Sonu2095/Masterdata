package com.avaya.amsp.masterdata.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.avaya.amsp.domain.PbxSystemSite;

public interface PbxSystemSiteRepository extends JpaRepository<PbxSystemSite, Long> {
	List<PbxSystemSite> findByPbxSystem_id(Long pbxSystemId);

	List<PbxSystemSite> findBySite_id(Long siteId);

	int deleteAllByPbxSystem_id(Long pbxSystemId);

	List<PbxSystemSite> findByPbxSystem_idAndActiveTrue(Long pbxSystemId);
}
