package com.avaya.amsp.masterdata.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.PortType;
import com.avaya.amsp.masterdata.annotation.PBXTechAuditLog;
import com.avaya.amsp.masterdata.dtos.PortTypeDto;
import com.avaya.amsp.masterdata.exceptions.ResourceAlreadyExistsException;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.PortTypeRepository;
import com.avaya.amsp.masterdata.service.iface.PortTypeServiceIface;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class PortTypeService implements PortTypeServiceIface {

	@Autowired
	PortTypeRepository portTypeRepo;
	
	@Autowired
	ModelMapper mapper;

	@Override
	public List<PortTypeDto> fetchAllPortTypes() {
		log.info("fetching portytypes from database");
		
		List<PortTypeDto> dtos =  new ArrayList<PortTypeDto>();
		
		List<PortType> portTypes =  portTypeRepo.findAll();
		if(portTypes!=null && !portTypes.isEmpty()) {
			portTypes.forEach(portType -> {
			PortTypeDto dto = new PortTypeDto();
			dto.setId(portType.getId());
			dto.setName(portType.getName());
			dto.setType(portType.getType());
			dto.setDescription(portType.getDescription());
			dto.setPseudoFlag(portType.getPseudoFlag());
			dto.setHidden(portType.getHidden());
			dto.setUser(portType.getLogCreatedBy());
			dto.setCreatedOn(portType.getLogCreatedOn());
			dtos.add(dto);
		});
		}else {
			log.info("No portType records found..");
		}
		return dtos;
	}

	@PBXTechAuditLog(action = "Insert",entity = "PortType",functionality = "PBX Add New Port Type")
	@Override
	public void persistPortType(PortTypeDto dto) {
		
		if(fetchPortTypeByType(dto.getType()).isPresent()) {
			log.info("PortType with type {} is already exists ",dto.getType());
			throw new ResourceAlreadyExistsException(String.format("PortType with type %s is already exists", dto.getType()));
		}
	
		PortType domain = mapper.map(dto, PortType.class);
		domain.setActive(1);
		domain.setLogCreatedBy(dto.getUser());
		domain.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
		PortType record = portTypeRepo.save(domain);
		
		log.info("persisted record with Id {}", record.getId());
	}

	@PBXTechAuditLog(action = "Update",entity = "PortType",functionality = "PBX Update Existing Port Type")
	@Override
	public void updatePortType(PortTypeDto dto) {
		log.info("updating portType record with ID {}",dto.getId());
	
		//check if portType with same name exist other than one being updated.
		Optional<PortType> existingPortType = fetchPortTypeByType(dto.getType());
		if(existingPortType.isPresent()) {
			PortType portType = existingPortType.get();
			if(portType.getId()!=dto.getId()) {
				log.info("PortType with type {} is already exists ",dto.getType());
				throw new ResourceAlreadyExistsException(String.format("PortType with type %s is already exists", dto.getType()));
			}
		}
		
		Optional<PortType> record = portTypeRepo.findById(dto.getId());
		record.ifPresentOrElse(value -> {
			value.setName(dto.getName());
			value.setDescription(dto.getDescription());
			value.setPseudoFlag(dto.getPseudoFlag());
			value.setHidden(dto.getHidden());
			value.setLogUpdatedBy(dto.getUser());
			value.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
			portTypeRepo.save(value);
		},()->{
			log.info("PortType record not found");
				throw new ResourceNotFoundException(String.format("portType with Id %s not found ", dto.getId()));
			});
	}

	@PBXTechAuditLog(action = "delete",entity = "PortType",functionality = "PBX delete Existing Port Type")
	@Override
	public void removePortType(Long portId) {
		
		log.info("Removing portType record with ID {}",portId);
		Optional<PortType> record = portTypeRepo.findById(portId);
		record.ifPresentOrElse(value -> {
			value.setActive(0);
			portTypeRepo.save(value);
		},()->{ 
			log.info("PortType record not found");
			throw new ResourceNotFoundException(String.format("portType with Id %s not found ", portId));
		});

	}

	@Override
	public Optional<PortType> fetchPortTypeByType(String type) {
		
		log.info("Fetching portType for name {}",type);
		PortType findByName = portTypeRepo.findByType(type);
		return Optional.ofNullable(findByName);
	}

}
