package com.avaya.amsp.masterdata.repo;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avaya.amsp.domain.ServiceCode;

public interface ServiceCodeRepository extends JpaRepository<ServiceCode, String>{

	ServiceCode findByServiceCode(String serviceCode);

	Optional<ServiceCode> findByServiceCodeIgnoreCase(String serviceCode);


}
