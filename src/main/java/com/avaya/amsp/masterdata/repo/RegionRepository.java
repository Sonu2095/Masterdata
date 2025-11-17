package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avaya.amsp.domain.Region;

public interface RegionRepository extends JpaRepository<Region, Long> {
	
	public Region findByName(String name);
	public List<Region> findByActive(Long active);
	
    @Query(value = "SELECT R.name FROM REGION R WHERE R.id = :id")
    String findNameById(Long id);

}
