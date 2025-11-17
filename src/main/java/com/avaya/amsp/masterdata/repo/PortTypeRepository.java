package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avaya.amsp.domain.PortType;

public interface PortTypeRepository extends JpaRepository<PortType, Long> {

	@Query("SELECT pt FROM PORT_TYPE pt WHERE pt.active = 1")
	List<PortType> findAll();

	public PortType findByType(String type);
}
