package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.avaya.amsp.domain.Shipping;
import com.avaya.amsp.domain.Site;

@Repository
public interface ShippingAddressRepository extends JpaRepository<Shipping, Long>, JpaSpecificationExecutor<Shipping> {

	@Query(value = "select S from SHIPPING S where S.site.id=:id and S.active=1")
	List<Shipping> findBySiteId(Long id);

	List<Shipping> findBySite(Site site);
	
	@Query(value = "select S from SHIPPING S where S.site.id=:id and S.active=1")
	Page<Shipping> findBySiteId(Long id, Pageable pageable);
}
