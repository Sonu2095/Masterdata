package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.avaya.amsp.domain.SitePool;
import com.avaya.amsp.masterdata.annotation.AuditLog;

public interface SitePoolRepository extends JpaRepository<SitePool, Long>, JpaSpecificationExecutor<SitePool> {
	
	@Query(value = "SELECT sp FROM SITE_POOL sp WHERE sp.site.id = :siteId AND sp.site.active = 1 AND sp.pool.active = 1")
	Page<SitePool> findBySite(Long siteId, Pageable pageable);
	
	@AuditLog(action = "Insert",entity = "SitePool",functionality = "Pool Assign to Site")
	default List<SitePool> addPoolsAssignToSite(List<SitePool> listSitePools){
		return saveAll(listSitePools);
	}
	
	@Query(value = "SELECT sp FROM SITE_POOL sp WHERE sp.site.id = :siteId AND sp.pool.id in :poolIds")
	List<SitePool> findAllBySiteIdAndPoolIds(Long siteId, List<Long> poolIds);
	
}
