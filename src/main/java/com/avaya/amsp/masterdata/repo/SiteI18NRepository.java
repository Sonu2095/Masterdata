package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.avaya.amsp.domain.SiteI18NName;

@Repository
public interface SiteI18NRepository extends JpaRepository<SiteI18NName, SiteI18NName.SiteI18NNameId> {
	
	@Query("SELECT N FROM SITE_I18N_NAME N WHERE N.site.id IN :siteIds")
	List<SiteI18NName> findBySiteIds(List<Long> siteIds);
	
}
