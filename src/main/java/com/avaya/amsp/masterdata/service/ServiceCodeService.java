package com.avaya.amsp.masterdata.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.ServiceCode;
import com.avaya.amsp.masterdata.dtos.ServiceCodeDto;
import com.avaya.amsp.masterdata.exceptions.ResourceAlreadyExistsException;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.ServiceCodeRepository;
import com.avaya.amsp.masterdata.service.iface.ServiceCodeServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServiceCodeService implements ServiceCodeServiceIface {

	@Autowired
	ServiceCodeRepository servicecodeRepo;

	@Autowired
	ModelMapper mapper;

	@Override
	public List<ServiceCodeDto> fetchAvailableServiceCode() {
		// TODO Auto-generated method stub
		log.info("fetching service from database");
		List<ServiceCodeDto> dtos = new ArrayList<ServiceCodeDto>();
		List<ServiceCode> serviceCode = servicecodeRepo.findAll();
		if (serviceCode != null && !serviceCode.isEmpty()) {
			serviceCode.forEach(service -> {
				ServiceCodeDto dto = mapper.map(service, ServiceCodeDto.class);
				dto.setServiceCode(service.getServiceCode());
				dto.setServiceCodeDescription(service.getServiceCodeDescription());
				dto.setServiceCodeDescriptionEngl(service.getServiceCodeDescriptionEngl());
				dto.setLongText(service.getLongText());
				dto.setLongTextEngl(service.getLongTextEngl());
				dto.setScoutKey(service.getScoutKey());
				dto.setServicecodeForConnectionFee(service.getServicecodeForConnectionFee());
				dto.setAccountingApproach(service.getAccountingApproach());
				dto.setAccountingApproachEngl(service.getAccountingApproachEngl());
				dtos.add(dto);
			});
		} else {
			log.info("No service records found...");
		}
		return dtos;
	}

	@Override
	public String persistService(ServiceCodeDto serviceDto, String userName) {
		if (fechServiceCode(serviceDto.getServiceCode()).isPresent()) {
			log.info("servicecode already exists for given request {} ", serviceDto.getServiceCode());
			return "servicecode already exists for given request "+serviceDto.getServiceCode();
		}

		log.info("adding new service to database");

		ServiceCode serviceData = mapper.map(serviceDto, ServiceCode.class);
		serviceData.setLogCreatedBy(userName);
		serviceData.setCreatedTimeStamp(LocalDateTime.now());
		serviceData.setLogUpdatedBy(userName);
		serviceData.setUpdatedTimeStamp(LocalDateTime.now());
		ServiceCode serviceRecord = servicecodeRepo.save(serviceData);
		log.info("added servicecode having id {}", serviceRecord.getServiceCode());
		return "created request";
	}

	@Override
	public Optional<ServiceCode> fechServiceCode(String serviceCode) {
		log.info("Fetching service with serviceCode {}", serviceCode);
		return servicecodeRepo.findByServiceCodeIgnoreCase(serviceCode);
		
	}

	@Override
	public void updateServiceCode(ServiceCodeDto servicecodeDto, String userName) {
		// TODO Auto-generated method stub
		log.info("updating servicecode record with ID {}", servicecodeDto.getServiceCode());
		Optional<ServiceCode> record = fechServiceCode(servicecodeDto.getServiceCode());
		record.ifPresentOrElse(value -> {
			value.setServiceCodeDescription(servicecodeDto.getServiceCodeDescription());
			value.setServiceCodeDescriptionEngl(servicecodeDto.getServiceCodeDescriptionEngl());
			value.setLongText(servicecodeDto.getLongText());
			value.setLogUpdatedBy(userName);
			value.setUpdatedTimeStamp(LocalDateTime.now());
			value.setLongTextEngl(servicecodeDto.getLongText());
			value.setScoutKey(servicecodeDto.getScoutKey());
			value.setServicecodeForConnectionFee(servicecodeDto.getServicecodeForConnectionFee());
			value.setAccountingApproach(servicecodeDto.getAccountingApproach());
			value.setAccountingApproachEngl(servicecodeDto.getAccountingApproachEngl());
			servicecodeRepo.save(value);
		}, () -> {
			log.info("servicecode record not found");
			throw new ResourceNotFoundException(
					String.format("servicecode with Id %s not found ", servicecodeDto.getServiceCode()));
		});

	}

	@Override
	public void deleteServiceCode(String serviceCode) {

		log.info("Removing servicecode record with ID {}", serviceCode);
		Optional<ServiceCode> record = fechServiceCode(serviceCode);
		record.ifPresentOrElse(value -> {
			servicecodeRepo.deleteById(serviceCode);
		}, () -> {
			log.info("servicecode record not found");
			throw new ResourceNotFoundException(String.format("servicecode with Id %s not found ", serviceCode));
		});

	}

}
