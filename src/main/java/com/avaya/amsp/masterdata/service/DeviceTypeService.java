package com.avaya.amsp.masterdata.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.DeviceType;
import com.avaya.amsp.masterdata.annotation.PBXTechAuditLog;
import com.avaya.amsp.masterdata.dtos.DeviceTypeDto;
import com.avaya.amsp.masterdata.repo.ClusterRepository;
import com.avaya.amsp.masterdata.repo.DeviceTypeRepository;
import com.avaya.amsp.masterdata.service.iface.DeviceTypeServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class DeviceTypeService implements DeviceTypeServiceIface {

	@Autowired
	private DeviceTypeRepository deviceTypeRepo;

	@Autowired
	private ClusterRepository clusterRepo;
	
	@Autowired
	private ModelMapper mapper;
	
	@PBXTechAuditLog(action = "delete",entity = "DeviceType",functionality = "PBX delete Device Type")
	@Override
	public boolean deleteDeviceType(long id) {
		deviceTypeRepo.deleteById(id);
		return true;
	}

	@Override
	public List<DeviceTypeDto> getDeviceTypes() {
		Iterable<DeviceType> deviceTypes = deviceTypeRepo.findAll();
		ArrayList<DeviceTypeDto> deviceTypesDto = new ArrayList<>();
		for(DeviceType deviceType : deviceTypes) {
			log.debug("Fetched " + deviceType + " from DB");
			deviceTypesDto.add(mapper.map(deviceType, DeviceTypeDto.class));
		}
		
		log.info("Got list : {}", deviceTypesDto);
		return deviceTypesDto;
	}

	@Override
	public List<DeviceTypeDto> searchDeviceTypeBy(DeviceTypeDto deviceTypeDto) {
		Example<DeviceType> example = Example.of(mapper.map(deviceTypeDto, DeviceType.class));
		log.debug("Example : {}", example);
		List<DeviceType> deviceTypes = deviceTypeRepo.findAll(example);
		log.info("Got deviceTypes count: {}", deviceTypes.size());
		ArrayList<DeviceTypeDto> deviceTypesDto = new ArrayList<>();
		for(DeviceType deviceType : deviceTypes) {
			log.debug("Fetched " + deviceType + " from DB");
			deviceTypesDto.add(mapper.map(deviceType, DeviceTypeDto.class));
		}
		
		return deviceTypesDto;
		
	}

	@Override
	public DeviceTypeDto getDeviceType(Long id) {
		Optional<DeviceType> deviceType = deviceTypeRepo.findById(id);
		DeviceTypeDto deviceTypeDto = null;
		if(deviceType.isPresent()) {
			deviceTypeDto = mapper.map(deviceType.get(), DeviceTypeDto.class);
		}
		log.debug("Got deviceTypeDto: {}", deviceTypeDto);
		return deviceTypeDto;
	}

	@Override
	public List<DeviceTypeDto> getDeviceTypesByCluster(Long id) {
		List<DeviceType> deviceTypes = deviceTypeRepo.findByClusterItemId(id);
		log.info("Got deviceTypes count: {} for clusterId {}", deviceTypes.size(), id);
		ArrayList<DeviceTypeDto> deviceTypesDto = new ArrayList<>();
		for(DeviceType deviceType : deviceTypes) {
			log.debug("Fetched " + deviceTypes + " from DB");
			deviceTypesDto.add(mapper.map(deviceType, DeviceTypeDto.class));
		}
		
		return deviceTypesDto;
	}

	@Override
	public boolean updateDeviceType(DeviceTypeDto deviceTypeDto) {
		Timestamp ts = Timestamp.valueOf(LocalDateTime.now());
		DeviceType deviceType = deviceTypeRepo.findById(deviceTypeDto.getId()).get();
		ClusterItem clusterItem = clusterRepo.getReferenceById(deviceTypeDto.getClusterItemId());
		deviceType.setClusterItem(clusterItem);
		BeanUtils.copyProperties(deviceTypeDto, deviceType,new String[] {"logCreatedOn","logCreatedBy"});
		deviceType.setLogUpdatedOn(ts);
		log.info("Update deviceType as {}", deviceType);
		deviceType = deviceTypeRepo.save(deviceType);
		return true;	
	}
	
	@PBXTechAuditLog(action = "update",entity = "DeviceType",functionality = "PBX update existing Device Type")
	@Override
	@org.springframework.transaction.annotation.Transactional
	public boolean saveDeviceType(DeviceTypeDto deviceTypeDto) {
		DeviceType deviceType = new DeviceType();
		Timestamp ts = Timestamp.valueOf(LocalDateTime.now());
		
		ClusterItem cluster = clusterRepo.findById(deviceTypeDto.getClusterItemId()).get();
		deviceType.setClusterItem(cluster);
		
		deviceType.setDescription(deviceTypeDto.getDescription());
		deviceType.setName(deviceTypeDto.getName());
		deviceType.setLogCreatedBy(deviceTypeDto.getLogCreatedBy());
		deviceType.setLogCreatedOn(ts);
		log.info("Saving deviceType as {}", deviceType);
		deviceType = deviceTypeRepo.save(deviceType);
		return true;
	}	

}
