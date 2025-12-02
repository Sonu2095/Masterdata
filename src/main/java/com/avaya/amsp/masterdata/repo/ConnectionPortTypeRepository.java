package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avaya.amsp.domain.ConnectionPortType;
import com.avaya.amsp.masterdata.annotation.AuditLog;

public interface ConnectionPortTypeRepository extends JpaRepository<ConnectionPortType, Long>{
	
	//@AuditLog(action = "INSERT",entity = "ConnectionPortType",functionality = "Save All Connections Port Type")
	default List<ConnectionPortType> saveAllConnectionPortTypes(List<ConnectionPortType> connPortTypes){
		return saveAll(connPortTypes);
	}
		
}