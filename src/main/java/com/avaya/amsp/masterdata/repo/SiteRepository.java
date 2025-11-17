package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avaya.amsp.domain.Site;

public interface SiteRepository extends JpaRepository<Site, Long> {

	public Site findByName(String name);

	public List<Site> findByActive(Long active);

	@Query(value = "select C from SITE  C LEFT JOIN FETCH C.clusterItem  where C.active=1 AND C.clusterItem.id= :id")
	public List<Site> findByClusterItem(Long id);
	
	@Query(value = "select C from SITE  C LEFT JOIN FETCH C.clusterItem  where C.active=1 AND C.clusterItem.id= :id")
	public Page<Site> findByClusterId(Long id, Pageable pageable);
	
    @Query("SELECT p.name FROM SITE p WHERE p.id = :id")
    String findNameById(Long id);
    
    @Query("SELECT s FROM SITE s WHERE s.id = :id AND s.active = 1")
    public Site findActiveById(Long id);

}
