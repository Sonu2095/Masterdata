package com.avaya.amsp.masterdata.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avaya.amsp.domain.ServiceType;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long>{

}
