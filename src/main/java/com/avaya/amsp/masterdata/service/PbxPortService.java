package com.avaya.amsp.masterdata.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.PbxPort;
import com.avaya.amsp.domain.PbxSystem;
import com.avaya.amsp.domain.PortType;
import com.avaya.amsp.masterdata.annotation.PBXTechAuditLog;
import com.avaya.amsp.masterdata.dtos.PbxPortDto;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.PbxPortRepository;
import com.avaya.amsp.masterdata.repo.PbxSystemRepository;
import com.avaya.amsp.masterdata.service.iface.PbxPortServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PbxPortService implements PbxPortServiceIface {
	@Autowired
	private PbxPortRepository pbxPortRepo;

	@Autowired
	private PbxSystemRepository pbxSystemRepo;

	@Autowired
	private ModelMapper mapper;

	@Override
	public List<PbxPortDto> fetchAllPbxPorts() {
		log.info("Fetching all special ports");
		List<PbxPortDto> PortDtos = new ArrayList<>();
		List<PbxPort> Ports = pbxPortRepo.findAll();
		for (PbxPort sp : Ports) {
			if (sp.getActive() != 0) {
				PbxPortDto portDto = mapper.map(sp, PbxPortDto.class);
				portDto.setStatus(portDto.getStatus());
				portDto.setIdPbxSystem(sp.getPbxSystem().getId());
				portDto.setIdCluster(sp.getCluster().getId());
				portDto.setIdPortType(sp.getPortType().getId());
				portDto.setIdPortTypeAem(sp.getPortType().getId());
				portDto.setPortName(sp.getPortType().getName());
				PortDtos.add(portDto);
			}
		}
		return PortDtos;
	}

	@PBXTechAuditLog(action = "insert",entity = "PbxPort",functionality = "PBX Add new Port")
	@Override
	public long savePbxPort(PbxPortDto pbxPortDto) {
		log.info("Saving pbxPortDto {}", pbxPortDto);

		//PbxPort psp = mapper.map(pbxPortDto, PbxPort.class);
		PbxPort psp = new PbxPort();
		psp.setLogCreatedBy(pbxPortDto.getLogCreatedBy());
		psp.setLogUpdatedBy(pbxPortDto.getLogCreatedBy());
		psp.setNetPanel(pbxPortDto.getNetPanel());
		psp.setNetPanelPort(pbxPortDto.getNetPanelPort());
		psp.setPbxPanel(pbxPortDto.getPbxPanel());
		psp.setRemark(pbxPortDto.getRemark());
		psp.setPbxPanelPort(pbxPortDto.getPbxPanelPort());
		psp.setPortName(pbxPortDto.getPortName());

		Optional<PbxSystem> pbxclu = pbxSystemRepo.findById(pbxPortDto.getIdPbxSystem());
		pbxclu.ifPresentOrElse(value -> {
			psp.setIdPbxCluster(value.getPbxCluster().getId());
		}, () -> {
			log.info("pbx system not found");
			throw new ResourceNotFoundException(
					String.format("pbxSystem with Id %s is  inactive or deleted", pbxPortDto.getIdPbxSystem()));
		});

		PbxSystem pbxsystem = new PbxSystem();
		pbxsystem.setId(pbxPortDto.getIdPbxSystem());
		psp.setPbxSystem(pbxsystem);

		ClusterItem cluster = new ClusterItem();
		cluster.setId(pbxPortDto.getIdCluster());
		psp.setCluster(cluster);

		PortType port = new PortType();
		port.setId(pbxPortDto.getIdPortType());
		psp.setPortType(port);
		psp.setAemPortType(port);

		// psp.setStatus(0);

		psp.setStatus(pbxPortDto.getStatus());
		psp.setActive(1);
		psp.setLogCreatedOn(LocalDateTime.now());
		psp.setLogUpdatedOn(LocalDateTime.now());
		PbxPort savedPsp = pbxPortRepo.save(psp);
		return savedPsp.getId();
	}

	@PBXTechAuditLog(action = "update",entity = "PbxPort",functionality = "PBX update existing new Port")
	@Override
	public void updatePbxPort(PbxPortDto pbxPortDto) {
		log.info("Updating pbxPortDto {}", pbxPortDto);
		Optional<PbxPort> exist = pbxPortRepo.findById(pbxPortDto.getId());

		if (exist.isPresent() && exist.get().getActive() != 0) {

			PbxPort psp = mapper.map(pbxPortDto, PbxPort.class);

			Optional<PbxSystem> pbxclu = pbxSystemRepo.findById(pbxPortDto.getIdPbxSystem());
			pbxclu.ifPresentOrElse(value -> {
				psp.setIdPbxCluster(value.getPbxCluster().getId());
			}, () -> {
				log.info("pbx system not found");
				throw new ResourceNotFoundException(
						String.format("pbxSystem with Id %s is  inactive or deleted", pbxPortDto.getIdPbxSystem()));
			});
			PbxSystem pbxsystem = new PbxSystem();
			pbxsystem.setId(pbxPortDto.getIdPbxSystem());
			psp.setPbxSystem(pbxsystem);

			ClusterItem cluster = new ClusterItem();
			cluster.setId(pbxPortDto.getIdCluster());
			psp.setCluster(cluster);

			PortType port = new PortType();
			port.setId(pbxPortDto.getIdPortType());
			psp.setPortType(port);
			psp.setAemPortType(port);

			psp.setStatus(pbxPortDto.getStatus());
			psp.setActive(1);
			psp.setLogUpdatedOn(LocalDateTime.now());
			psp.setLogCreatedOn(exist.get().getLogCreatedOn());
			psp.setLogCreatedBy(exist.get().getLogCreatedBy());
			pbxPortRepo.save(psp);

		} else {
			throw new IllegalArgumentException(
					String.format("record is deleted or not in active state, so specific record can't be updated\""));
		}
	}

	@PBXTechAuditLog(action = "delete",entity = "PbxPort",functionality = "PBX delete new Port")
	@Override
	public void deletePbxPort(Long pbxPortId) {
		log.info("deleting pbx special port record with ID {}", pbxPortId);
		Optional<PbxPort> record = pbxPortRepo.findById(pbxPortId);
		record.ifPresentOrElse(value -> {
			value.setActive(0);
			pbxPortRepo.save(value);
		}, () -> {
			log.info("pbx port not found");
			throw new ResourceNotFoundException(String.format("pbxPort with Id %s not found ", pbxPortId));
		});
	}
}
