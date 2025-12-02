package com.avaya.amsp.masterdata.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.PbxComponent;
import com.avaya.amsp.domain.PbxSystem;
import com.avaya.amsp.masterdata.annotation.PBXTechAuditLog;
import com.avaya.amsp.masterdata.dtos.PbxComponentDto;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.PbxComponentRepository;
import com.avaya.amsp.masterdata.repo.PbxSystemRepository;
import com.avaya.amsp.masterdata.service.iface.PbxComponentServiceIface;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class PbxComponentService implements PbxComponentServiceIface {

	@Autowired
	private ModelMapper mapper;
	
	@Autowired
	private PbxComponentRepository pbxComponentRepo;

	@Autowired
	private PbxSystemRepository pbxSystemRepo;

	@Override
	public List<PbxComponentDto> fetchAllPbxComponent() {
		log.info("fetching pbxComponents");	
		List<PbxComponentDto> dtos = new ArrayList<PbxComponentDto>();
		List<PbxComponent> pbxComponents = pbxComponentRepo.findAll();
		
		if (pbxComponents != null && !pbxComponents.isEmpty()) {
			pbxComponents.forEach(pbxComponent -> {
				if (pbxComponent.getActive() != 0) {
					PbxComponentDto dto = mapper.map(pbxComponent, PbxComponentDto.class);
					dto.setIdCluster(pbxComponent.getClusterItem().getId());
					dto.setIdPbxSystem(pbxComponent.getPbxSystem().getId());
					dtos.add(dto);
				}
			});
		} else {
			log.info("No pbxComonent records found..");
		}
		return dtos;
	}
	
	@Override
	@PBXTechAuditLog(action = "insert",entity = "PbxComponent",functionality = "PBX Add PbxComponent")
	@org.springframework.transaction.annotation.Transactional
	public long savePbxComponent(PbxComponentDto pbxComponentDto) {	
		log.info("Saving pbxComponent {}", pbxComponentDto);	
		if(pbxComponentDto.getIdCluster() == null  || pbxComponentDto.getIdCluster() == 0) {
			throw new IllegalArgumentException(String.format("Cluster id can't be empty",pbxComponentDto.getIdCluster()));					
		}
		
		if(pbxComponentDto.getIdPbxSystem() == null  || pbxComponentDto.getIdPbxSystem() == 0) {
			throw new IllegalArgumentException(String.format("PbxSystem id can't be empty",pbxComponentDto.getIdPbxSystem()));					
		}

		Optional<PbxSystem> pbxSystem = pbxSystemRepo.findById(pbxComponentDto.getIdPbxSystem());

		if(pbxSystem.isPresent()) {
			Long clusterId = pbxSystem.get().getPbxCluster().getClusterItem().getId();
			
			if(clusterId.equals(pbxComponentDto.getIdCluster()) ) {
				
				List<PbxComponent> pbxHwa = pbxComponentRepo.findByHwa(pbxComponentDto.getHwa());
				
				pbxHwa.forEach(pbxComponentRecord -> {
					if( pbxComponentRecord.getClusterItem().getId().equals(pbxComponentDto.getIdCluster())  &&
							pbxComponentRecord.getPbxSystem().getId().equals(pbxComponentDto.getIdPbxSystem()) && 
							pbxComponentRecord.getHwa().equals(pbxComponentDto.getHwa())){
						throw new IllegalArgumentException(String.format("Record already present"));					
					}
				});
				
				//PbxComponent pc = mapper.map(pbxComponentDto, PbxComponent.class);
				
				PbxComponent pc = new PbxComponent();
				
				pc.setHwa(pbxComponentDto.getHwa());
				pc.setRemark(pbxComponentDto.getRemark());
				
				ClusterItem cluster = new ClusterItem();
				cluster.setId(pbxComponentDto.getIdCluster());
				pc.setClusterItem(cluster);
				
				PbxSystem pbxsystem = new PbxSystem();
				pbxsystem.setId(pbxComponentDto.getIdPbxSystem());
				pc.setPbxSystem(pbxsystem);
				
				pc.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));	
				pc.setLogCreatedBy(pbxComponentDto.getLogCreatedBy());	
				pc.setActive(1);

				PbxComponent savedPc = pbxComponentRepo.save(pc);
				return savedPc.getId();
				
			}else {	
				throw new IllegalArgumentException(String.format("PbxSystem with Id %s is not part of selected cluster",pbxComponentDto.getIdPbxSystem()));					
			}
			
		}else {
			throw new IllegalArgumentException(String.format("PbxSystem with Id %s is not part of selected cluster",pbxComponentDto.getIdPbxSystem()));					
		}
	}

	@Override
	@PBXTechAuditLog(action = "update",entity = "PbxComponent",functionality = "PBX update existing PbxComponent")
	public void updatePbxComponent(PbxComponentDto pbxComponentDto) {		
		log.info("Updating pbxComponent {}", pbxComponentDto);
				
		Optional<PbxComponent> pbxcomp = pbxComponentRepo.findById(pbxComponentDto.getId());
		
		if(pbxcomp.isPresent() && pbxcomp.get().getActive() != 0) {
	
			if(pbxComponentDto.getId() == null  || pbxComponentDto.getId() == 0) {
				throw new IllegalArgumentException(String.format("PbxComponent Id can't be empty"));					
			}
			
			if(pbxComponentDto.getIdCluster() == null  || pbxComponentDto.getIdCluster() == 0) {
				throw new IllegalArgumentException(String.format("Cluster Id can't be empty"));					
			}
			
			if(pbxComponentDto.getIdPbxSystem() == null  || pbxComponentDto.getIdPbxSystem() == 0) {
				throw new IllegalArgumentException(String.format("PbxSystem Id can't be empty"));					
			}
	
			Optional<PbxSystem> pbxSystem = pbxSystemRepo.findById(pbxComponentDto.getIdPbxSystem());
			
			if(pbxSystem.isPresent()) {
				Long clusterId = pbxSystem.get().getPbxCluster().getClusterItem().getId();
				if(clusterId.equals(pbxComponentDto.getIdCluster()) ) {		
					PbxComponent pc = mapper.map(pbxComponentDto, PbxComponent.class);
					ClusterItem cluster = new ClusterItem();
					cluster.setId(pbxComponentDto.getIdCluster());
					pc.setClusterItem(cluster);
					
					PbxSystem pbxsystem = new PbxSystem();
					pbxsystem.setId(pbxComponentDto.getIdPbxSystem());
					pc.setPbxSystem(pbxsystem);
					pc.setLogCreatedBy(pbxcomp.get().getLogCreatedBy());
					pc.setLogCreatedOn(pbxcomp.get().getLogCreatedOn());
					pc.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
					pc.setActive(1);
					
					pbxComponentRepo.save(pc);
					
				}else {	
					throw new IllegalArgumentException(String.format("PbxSystem with Id %s is not part of selected cluster",pbxComponentDto.getIdPbxSystem()));					
				}
				
			}else {
				throw new IllegalArgumentException(String.format("PbxSystem with Id %s is not part of selected cluster",pbxComponentDto.getIdPbxSystem()));					
			}	
		
		}else {
			throw new IllegalArgumentException(String.format("record is deleted or not in active state, so specific record can't be updated"));					
		}
	}

	@Override
	@PBXTechAuditLog(action = "delete",entity = "PbxComponent",functionality = "PBX delete PbxComponent")
	public void deletePbxComponent(long idPbxComponent) {
		log.info("deleting pbxcomponent record with ID {}", idPbxComponent);
		Optional<PbxComponent> record = pbxComponentRepo.findById(idPbxComponent);
		record.ifPresentOrElse(value -> {
			value.setActive(0);
			pbxComponentRepo.save(value);
		}, () -> {
			log.info("pbx record not found");
			throw new ResourceNotFoundException(String.format("pbxcomponent with Id %s not found ", idPbxComponent));
		});
	}
}