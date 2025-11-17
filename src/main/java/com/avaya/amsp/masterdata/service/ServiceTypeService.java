package com.avaya.amsp.masterdata.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.DeviceType;
import com.avaya.amsp.domain.ServiceType;
import com.avaya.amsp.masterdata.annotation.PBXTechAuditLog;
import com.avaya.amsp.masterdata.dtos.DeviceTypeDto;
import com.avaya.amsp.masterdata.dtos.ServiceTypeDto;
import com.avaya.amsp.masterdata.repo.ServiceTypeRepository;
import com.avaya.amsp.masterdata.service.iface.ServiceTypeServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServiceTypeService implements ServiceTypeServiceIface{
	@Autowired
	private ServiceTypeRepository serviceTypeRepo;
	
	@Autowired
	private ModelMapper mapper;
	@Override
	public List<ServiceTypeDto> getAllServiceTypes() {
		List<ServiceType> serviceTypes = serviceTypeRepo.findAll();
		log.info("Got serviceType as {}", serviceTypes);
		List<ServiceTypeDto> serviceTypeDtos = new ArrayList<>();
		for(ServiceType serviceType : serviceTypes)
				serviceTypeDtos.add(mapper.map(serviceType, ServiceTypeDto.class));
		return serviceTypeDtos;
	}
	
	@PBXTechAuditLog(action = "update",entity = "ServiceType",functionality = "PBX update existing Service Type")
	@Override
	public boolean saveServiceType(ServiceTypeDto serviceTypeDto) {
		ServiceType serviceType = null;
		
		if(serviceTypeDto.getId() == 0) {
			serviceType = new ServiceType();
			serviceType.setName(serviceTypeDto.getName());
			serviceType.setDescription(serviceTypeDto.getDescription());
			serviceType.setLogCreatedBy(serviceTypeDto.getLogCreatedBy());
			serviceType.setLogCreatedOn(LocalDateTime.now());
		}else {
			Optional<ServiceType> optionalServiceType = serviceTypeRepo.findById(serviceTypeDto.getId());
			if(optionalServiceType.isPresent()) {
				serviceType = optionalServiceType.get();
				log.info("Updating serviceType as Id {}", serviceType.getId());
				serviceType.setName(serviceTypeDto.getName());
				serviceType.setDescription(serviceTypeDto.getDescription());
				serviceType.setLogUpdatedBy(serviceTypeDto.getLogUpdatedBy()); 
				serviceType.setLogUpdatedOn(LocalDateTime.now());					
			}
		}
		
		log.info("Saving serviceType as {}", serviceType);
		serviceType = serviceTypeRepo.save(serviceType);
		return true;
	}
	@PBXTechAuditLog(action = "delete",entity = "ServiceType",functionality = "PBX delete Service Type")
	@Override
	public boolean deleteServiceType(long id) {
		serviceTypeRepo.deleteById(id);
		return true;
		
	}

}
