package com.avaya.amsp.masterdata.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.PbxNumberRange;
import com.avaya.amsp.domain.PbxSpecialPort;
import com.avaya.amsp.domain.PbxSystem;
import com.avaya.amsp.domain.PortType;
import com.avaya.amsp.masterdata.annotation.PBXTechAuditLog;
import com.avaya.amsp.masterdata.dtos.PbxSpecialPortDto;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.PbxSpecialPortRepository;
import com.avaya.amsp.masterdata.service.iface.PbxSpecialPortServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PbxSpecialPortService implements PbxSpecialPortServiceIface {
	@Autowired
	private PbxSpecialPortRepository pbxSpecialPortRepo;
	
	@Autowired
	private ModelMapper mapper;
	
	@Override
	public List<PbxSpecialPortDto> fetchAllPbxSpecialPorts() {
		log.info("Fetching all special ports");
		List<PbxSpecialPortDto> specialPortDtos = new ArrayList<>();
		List<PbxSpecialPort> specialPorts = pbxSpecialPortRepo.findAll();
		for(PbxSpecialPort sp : specialPorts) {
			if (sp.getActive() != 0) {
				PbxSpecialPortDto psp = mapper.map(sp, PbxSpecialPortDto.class);
				
				psp.setIdPbxSystem(sp.getPbxSystem().getId());
				psp.setIdCluster(sp.getCluster().getId());
				psp.setIdPortType(sp.getPortType().getId());
				psp.setIdPortTypeAem(sp.getPortType().getId());
				specialPortDtos.add(psp);
			}
		}
		return specialPortDtos;
	}

	@PBXTechAuditLog(action = "Insert",entity = "PbxSpecialPort",functionality = "PBX Add New Special Port")
	@Override
	public long savePbxSpecialPort(PbxSpecialPortDto pbxSpecialPortDto) {
		log.info("Saving pbxSpecialPortDto {}", pbxSpecialPortDto);
		
		PbxSpecialPort psp = new PbxSpecialPort();
		psp.setHwa(pbxSpecialPortDto.getHwa());
		psp.setRemark(pbxSpecialPortDto.getRemark());
		psp.setLogCreatedBy(pbxSpecialPortDto.getLogCreatedBy());
		
		PbxSystem pbxsystem = new PbxSystem();
		pbxsystem.setId(pbxSpecialPortDto.getIdPbxSystem());
		psp.setPbxSystem(pbxsystem);
		
		ClusterItem cluster = new ClusterItem();
		cluster.setId(pbxSpecialPortDto.getIdCluster());
		psp.setCluster(cluster);
		
		PortType port = new PortType();
		port.setId(pbxSpecialPortDto.getIdPortType());
		psp.setPortType(port);
		psp.setAemPortType(port);
		
		psp.setActive(1);
		psp.setLogCreatedOn(LocalDateTime.now());
		PbxSpecialPort savedPsp = pbxSpecialPortRepo.save(psp);
		return savedPsp.getId();
	}

	@PBXTechAuditLog(action = "Update",entity = "PbxSpecialPort",functionality = "PBX Update Existing Special Port")
	@Override
	public void updatePbxSpecialPort(PbxSpecialPortDto pbxSpecialPortDto) {
		log.info("Updating pbxSpecialPortDto {}", pbxSpecialPortDto);
		Optional<PbxSpecialPort> exist = pbxSpecialPortRepo.findById(pbxSpecialPortDto.getId());
		
		if(exist.isPresent() && exist.get().getActive() != 0 ) {
			
			PbxSpecialPort psp = mapper.map(pbxSpecialPortDto, PbxSpecialPort.class);
			
			PbxSystem pbxsystem = new PbxSystem();
			pbxsystem.setId(pbxSpecialPortDto.getIdPbxSystem());
			psp.setPbxSystem(pbxsystem);
			
			ClusterItem cluster = new ClusterItem();
			cluster.setId(pbxSpecialPortDto.getIdCluster());
			psp.setCluster(cluster);
			
			PortType port = new PortType();
			port.setId(pbxSpecialPortDto.getIdPortType());
			psp.setPortType(port);
			psp.setAemPortType(port);
			
			psp.setActive(1);
			psp.setLogUpdatedOn(LocalDateTime.now());
			psp.setLogCreatedOn(exist.get().getLogCreatedOn());
			psp.setLogCreatedBy(exist.get().getLogCreatedBy());
			pbxSpecialPortRepo.save(psp);

		}else {
			throw new IllegalArgumentException(String.format("record is deleted or not in active state, so specific record can't be updated\""));
		}		
	}
	
	@PBXTechAuditLog(action = "delete",entity = "PbxSpecialPort",functionality = "PBX delete Special Port")
	@Override
	public void deletePbxSpecialPort(Long pbxSpecialPortId) {
		log.info("deleting pbx special port record with ID {}", pbxSpecialPortId);
		Optional<PbxSpecialPort> record = pbxSpecialPortRepo.findById(pbxSpecialPortId);
		record.ifPresentOrElse(value -> {
			value.setActive(0);
			pbxSpecialPortRepo.save(value);
		}, () -> {
			log.info("pbx special port not found");
			throw new ResourceNotFoundException(String.format("pbxSpecialPort with Id %s not found ", pbxSpecialPortId));
		});
	}
}
