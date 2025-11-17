package com.avaya.amsp.masterdata.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avaya.amsp.domain.PbxCluster;
import com.avaya.amsp.domain.PbxComponent;
import com.avaya.amsp.domain.PbxPort;
import com.avaya.amsp.domain.PbxSpecialPort;
import com.avaya.amsp.domain.PbxSystem;
import com.avaya.amsp.domain.PbxSystemSite;
import com.avaya.amsp.domain.Site;
import com.avaya.amsp.masterdata.annotation.PBXTechAuditLog;
import com.avaya.amsp.masterdata.dtos.AemPbxDto;
import com.avaya.amsp.masterdata.dtos.PbxComponentDto;
import com.avaya.amsp.masterdata.dtos.PbxPortDto;
import com.avaya.amsp.masterdata.dtos.PbxSpecialPortDto;
import com.avaya.amsp.masterdata.dtos.PbxSystemDto;
import com.avaya.amsp.masterdata.dtos.PbxSystemSiteDto;
import com.avaya.amsp.masterdata.dtos.SiteDto;
import com.avaya.amsp.masterdata.exceptions.ResourceAlreadyExistsException;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.PbxClusterRepository;
import com.avaya.amsp.masterdata.repo.PbxComponentRepository;
import com.avaya.amsp.masterdata.repo.PbxPortRepository;
import com.avaya.amsp.masterdata.repo.PbxSpecialPortRepository;
import com.avaya.amsp.masterdata.repo.PbxSystemRepository;
import com.avaya.amsp.masterdata.repo.PbxSystemSiteRepository;
import com.avaya.amsp.masterdata.repo.SiteRepository;
import com.avaya.amsp.masterdata.service.iface.PbxSystemServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PbxSystemService implements PbxSystemServiceIface {
	@Autowired
	private PbxSystemRepository pbxSystemRepo;
	
	@Autowired
	PbxClusterRepository pbxClusterRepo;
	
	@Autowired
	SiteRepository siteRepo;
	
	@Autowired
	PbxSystemSiteRepository pbxSystemSiteRepo;
	
	@Autowired
	PbxComponentRepository pbxComponentRepo;
		
	@Autowired
	PbxSpecialPortRepository pbxSpecialPortRepo;
	
	@Autowired
	PbxPortRepository pbxPortRepo;
	
	@Autowired
	private ModelMapper mapper;
	
	@Override
	public List<PbxSystemDto> fetchAllPbxSystems() {
		
		log.info("fetching pbxsystems from database");

		List<PbxSystemDto> dtos = new ArrayList<PbxSystemDto>();

		List<PbxSystem> pbxsystems = pbxSystemRepo.findAll();
		
		if (pbxsystems != null && !pbxsystems.isEmpty()) {

			pbxsystems.forEach(pbxsystem -> {
				if (pbxsystem.getActive() != 0) {
					PbxSystemDto dto = mapper.map(pbxsystem, PbxSystemDto.class);
					if (pbxsystem.getpbxSystemSite() != null) {
						dto.setSites(new ArrayList<>());
						pbxsystem.getpbxSystemSite().stream().forEach(pbxSystemSite -> {
							dto.getSites().add(mapper.map(pbxSystemSite.getSite(), SiteDto.class));
						});
	
					}
					dto.setLogCreatedBy(pbxsystem.getLogCreatedBy());
					dto.setLogUpdatedBy(pbxsystem.getLogUpdatedBy());
	
					dtos.add(dto);
				}
			});
		} else {
			log.info("No pbxSystem records found..");
		}
		return dtos;
	}

	@Override
	@PBXTechAuditLog(action = "insert",entity = "PbxSystem",functionality = "PBX Add PbxSystem")
	@org.springframework.transaction.annotation.Transactional
	public void savePbxSystem(PbxSystemDto pbxSystemDto) {
		
		log.info("Persisting pbxsystem to database");

		if (fetchPbxSystemByPhysicalPbx(pbxSystemDto.getPhysicalPbx()).isPresent()) {
			log.info("PbxSystem with name {} is already exists ", pbxSystemDto.getPhysicalPbx());
			throw new ResourceAlreadyExistsException(
					String.format("PbxSystem with name %s is already exists", pbxSystemDto.getPhysicalPbx()));
		}
		
		log.info("PbxSystem with name {} is already exists ", pbxSystemDto.getPhysicalPbx());

		//PbxSystem pbxsystem = mapper.map(pbxSystemDto, PbxSystem.class);
		PbxSystem pbxsystem = new PbxSystem();
		pbxsystem.setAemPbx(pbxSystemDto.getAemPbx());
		pbxsystem.setArsAnalysisEntry(pbxSystemDto.isArsAnalysisEntry());
		pbxsystem.setAssemblingEmailId(pbxSystemDto.getAssemblingEmailId());
		pbxsystem.setCmName(pbxSystemDto.getCmName());
		pbxsystem.setFlCode(pbxSystemDto.getFlCode());
		pbxsystem.setNotes(pbxSystemDto.getNotes());
		
		log.info("Checking if pbx cluster with Id {} exists or not", pbxSystemDto.getPbxCluster().getId());
		
		Optional<PbxCluster> pbxCluster = pbxClusterRepo.findById(pbxSystemDto.getPbxCluster().getId());

		pbxCluster.ifPresentOrElse(record -> log.info("PBX cluster record found"), () -> {
			throw new IllegalArgumentException(String.format("PBX cluster with Id %s not found", pbxSystemDto.getPbxCluster().getId()));
		});
		
		pbxsystem.setPbxCluster(pbxCluster.get());
		
		pbxsystem.setPhysicalPbx(pbxSystemDto.getPhysicalPbx());
		pbxsystem.setRemark(pbxSystemDto.getRemark());
		pbxsystem.setRoutingPolicyName(pbxSystemDto.getRoutingPolicyName());
		pbxsystem.setSfbsSystem(pbxSystemDto.isSfbsSystem());
		pbxsystem.setShippingEmailId(pbxSystemDto.getShippingEmailId());
		pbxsystem.setSipDomain(pbxSystemDto.getSipDomain());
		pbxsystem.setTeamsSystem(pbxSystemDto.isTeamsSystem());
		pbxsystem.setLogCreatedBy(pbxSystemDto.getLogCreatedBy());
		pbxsystem.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
		pbxsystem.setActive(1);
		
		List<Site> sites = siteRepo.findAllById(pbxSystemDto.getPbxSytemSiteIds());
		if(sites.isEmpty()) {
			log.info("Site can not be empty.");
			throw new ResourceAlreadyExistsException(
					String.format("Site can not be empty"));
		}
		
		PbxSystem record = pbxSystemRepo.save(pbxsystem);

		log.info("Inserting Site PbxSytem relationship");
		List<PbxSystemSite> newPbxSytemSites = new ArrayList<PbxSystemSite>();
		sites.forEach(site -> {
			PbxSystemSite pbxSystemSite = new PbxSystemSite();
			pbxSystemSite.setActive(1);
			pbxSystemSite.setSite(site);
			pbxSystemSite.setPbxSystem(record);
			newPbxSytemSites.add(pbxSystemSite);
		});
		pbxSystemSiteRepo.saveAll(newPbxSytemSites);

		log.info("persisted record with Id {}", record.getId());
	}

	
	@Override
	public Optional<PbxSystem> fetchPbxSystemByPhysicalPbx(String name) {
		log.info("Fetching pbxsystem for name {}", name);
		PbxSystem pbxsystem = pbxSystemRepo.findByPhysicalPbx(name);
		log.info("PbxO record with Id {}", pbxsystem);

		return Optional.ofNullable(pbxsystem);
	}
	
	@Override
	@PBXTechAuditLog(action = "update",entity = "PbxSystem",functionality = "PBX update existing PbxSystem")
	public void updatePbxSystem(PbxSystemDto pbxSystemDto) {
		
		log.info("updating pbxSystem with Id {}", pbxSystemDto.getId());

		//Check if connection with same name exists other than one being updated
		Optional<PbxSystem> existingPbxSystem = fetchPbxSystemByPhysicalPbx(pbxSystemDto.getPhysicalPbx());
		PbxSystem pbxSystem = null;
		if (existingPbxSystem.isPresent() && existingPbxSystem.get().getActive() != 0) {
			pbxSystem = existingPbxSystem.get();
			if (pbxSystem.getId() != pbxSystemDto.getId()) {
				log.info("PbxSystem with name {} is already exists ", pbxSystemDto.getPhysicalPbx());
				throw new ResourceAlreadyExistsException(
						String.format("PbxSystem with name %s is already exists", pbxSystemDto.getPhysicalPbx()));
			}
		}

		log.info("Checking if cluster with Id {} exists or not", pbxSystemDto.getPbxCluster().getId());
		
		Optional<PbxCluster> cluster = pbxClusterRepo.findById(pbxSystemDto.getPbxCluster().getId());

		cluster.ifPresentOrElse(record -> log.info("cluster record found"), () -> {
			throw new IllegalArgumentException(String.format("cluster with Id %s not found", pbxSystemDto.getPbxCluster().getId()));
		});
		

		//Check if input site do exist
		log.info("Checking given sites exists..");
		List<Site> sites = siteRepo.findAllById(pbxSystemDto.getPbxSytemSiteIds());
		if(sites.isEmpty()) {
			log.info("Site Ids can not be empty.");
			throw new ResourceAlreadyExistsException(
					String.format("Site Ids can not be empty"));
		}
		Optional<PbxSystem> record = pbxSystemRepo.findById(pbxSystemDto.getId());
		record.ifPresentOrElse(value -> {
			
			value.setPhysicalPbx(pbxSystemDto.getPhysicalPbx());
			value.setAemPbx(pbxSystemDto.getAemPbx());
			
			PbxCluster pbxcluster = new PbxCluster();
			pbxcluster.setId(pbxSystemDto.getPbxCluster().getId());
			value.setPbxCluster(pbxcluster);
			value.setActive(1);
			value.setFlCode(pbxSystemDto.getFlCode());
			value.setShippingEmailId(pbxSystemDto.getShippingEmailId());
			value.setSipDomain(pbxSystemDto.getSipDomain());
			value.setRemark(pbxSystemDto.getRemark());
			value.setRoutingPolicyName(pbxSystemDto.getRoutingPolicyName());			
			value.setCmName(pbxSystemDto.getCmName());
			value.setNotes(pbxSystemDto.getNotes());
			value.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
			value.setArsAnalysisEntry(pbxSystemDto.isArsAnalysisEntry());
			value.setSfbsSystem(pbxSystemDto.isSfbsSystem());
			value.setTeamsSystem(pbxSystemDto.isTeamsSystem());	
			value.setAssemblingEmailId(pbxSystemDto.getAssemblingEmailId());
			pbxSystemRepo.save(value);

			log.info("Deleting existing pbx-system site relationship");
			Set<PbxSystemSite> existingSitess = value.getpbxSystemSite();
			pbxSystemSiteRepo.deleteAll(existingSitess);

			log.info("Inserting pbx-system site relationship");
			List<PbxSystemSite> newSites = new ArrayList<PbxSystemSite>();
			sites.forEach(site -> {
				PbxSystemSite pbxSite = new PbxSystemSite();
				pbxSite.setActive(1);
				pbxSite.setPbxSystem(value);
				pbxSite.setSite(site);
				newSites.add(pbxSite);
			});
			pbxSystemSiteRepo.saveAll(newSites);

		}, () -> {
			log.info("PbxSystem record not found");
			throw new ResourceNotFoundException(String.format("PbxSystem with Id %s not found ", pbxSystemDto.getId()));
		});
	}

	@Override
	@Transactional
	@PBXTechAuditLog(action = "delete",entity = "PbxSystem",functionality = "PBX delete PbxSystem")
	public void deletePbxSystem(Long pbxSystemId) {
		Optional<PbxSystem> exist = pbxSystemRepo.findById(pbxSystemId);

		exist.ifPresentOrElse(value -> {
			
			List<PbxSystemSite> existSite = pbxSystemSiteRepo.findByPbxSystem_id(pbxSystemId);
			if(!existSite.isEmpty()) {
		        for (PbxSystemSite site : existSite) {
			        site.setActive(0);
			        pbxSystemSiteRepo.save(site);
		        }
			}
			value.setActive(0);
			pbxSystemRepo.save(value);
		}, () -> {
			log.info("pbx system not found");
			throw new ResourceNotFoundException(String.format("pbxSystem with Id %s not found ", pbxSystemId));
		});
	}

	@Override
	public AemPbxDto fetchAemPbxByPhysicalPbx(String physicalPbx) {
		Optional<PbxSystem> physicalPbxRec = fetchPbxSystemByPhysicalPbx(physicalPbx);
		if (physicalPbxRec.isPresent()) {
			AemPbxDto aempbx = new AemPbxDto();
			aempbx.setAemPbx(physicalPbxRec.get().getAemPbx());	
			return aempbx;
		}
		else {
			log.info("PhysicalPbx not found");
			throw new ResourceNotFoundException(String.format("PbxSystem record for physcialPbx: %s not found ", physicalPbx));			
		}
	}

	@Override
	public List<PbxComponentDto> fetchAllPbxComponentByPbxSystem(Long idPbxSystem) {
		List<PbxComponentDto> pbxcomps = new ArrayList<PbxComponentDto>();
		List<PbxComponent> records = pbxComponentRepo.findByPbxSystem_id(idPbxSystem);
		
		if (records != null && !records.isEmpty()) {

			records.forEach(pbxComp -> {

				if (pbxComp.getActive() != 0) {			
					PbxComponentDto pc = mapper.map(pbxComp, PbxComponentDto.class);
					pc.setIdCluster(pbxComp.getClusterItem().getId());
					pc.setIdPbxSystem(pbxComp.getPbxSystem().getId());
					pbxcomps.add(pc);
				}

			});
		} else {
			log.info("no active records found");
		}
		return pbxcomps;
	}	
	
	@Override
	public List<PbxSpecialPortDto> fetchAllPbxSpecialPortsByPbxSystem(Long idPbxSystem) {
		List<PbxSpecialPortDto> pbxspecialports = new ArrayList<PbxSpecialPortDto>();
		List<PbxSpecialPort> records = pbxSpecialPortRepo.findByPbxSystem_id(idPbxSystem);
		
		if (records != null && !records.isEmpty()) {

			records.forEach(pbxSpecialPorts -> {

				if (pbxSpecialPorts.getActive() != 0) {			
					PbxSpecialPortDto psp = mapper.map(pbxSpecialPorts, PbxSpecialPortDto.class);
					
					psp.setIdCluster(pbxSpecialPorts.getCluster().getId());
					psp.setIdPbxSystem(pbxSpecialPorts.getPbxSystem().getId());
					psp.setIdPortType(pbxSpecialPorts.getPortType().getId());
					psp.setIdPortTypeAem(pbxSpecialPorts.getPortType().getId());
					
					pbxspecialports.add(psp);
				}

			});
		} else {
			log.info("no active records found");
		}
		return pbxspecialports;
	}

	@Override
	public List<PbxPortDto> fetchAllPbxPortsByPbxSystem(Long idPbxSystem) {
		List<PbxPortDto> pbxports = new ArrayList<PbxPortDto>();
		List<PbxPort> records = pbxPortRepo.findByPbxSystem_id(idPbxSystem);
		
		if (records != null && !records.isEmpty()) {

			records.forEach(pbxPorts -> {

				if (pbxPorts.getActive() != 0) {			
					PbxPortDto psp = mapper.map(pbxPorts, PbxPortDto.class);
					
					psp.setIdCluster(pbxPorts.getCluster().getId());
					psp.setIdPbxSystem(pbxPorts.getPbxSystem().getId());
					psp.setIdPortType(pbxPorts.getPortType().getId());
					psp.setIdPortTypeAem(pbxPorts.getPortType().getId());
					
					pbxports.add(psp);
				}

			});
		} else {
			log.info("no active records found");
		}
		return pbxports;
	}
	
	@Override
	public List<PbxSystemSiteDto> fetchAllPbxSystemBySiteId(Long siteId) {
		
		List<PbxSystemSiteDto> dtos = new ArrayList<PbxSystemSiteDto>();
		
		List<PbxSystemSite> pbxSystemSiteList = pbxSystemSiteRepo.findBySite_id(siteId);
		if (pbxSystemSiteList != null && !pbxSystemSiteList.isEmpty()) {
			
			pbxSystemSiteList.forEach(pbxSystem -> {				
				if (pbxSystem.getActive() != 0) {			
					PbxSystemSiteDto pbxSystemSiteDto = mapper.map(pbxSystem.getPbxSystem(), PbxSystemSiteDto.class);														
					dtos.add(pbxSystemSiteDto);
				}
			});			
		} else {
			log.info("no active records found");
		}
		
		return dtos;
		
	}
}
