package com.avaya.amsp.masterdata.repo;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.avaya.amsp.domain.Site;
import com.avaya.amsp.domain.SiteContractFL;

@Repository
public interface SiteContractFlRepository extends JpaRepository<SiteContractFL, Long> {

	List<SiteContractFL> findBySite(Site site);
	
//	public List<SiteContractFl> findByKeyIgnoreCaseAndActive(String key,int active);
	
}
