package com.avaya.amsp.masterdata.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.PbxCluster;
import com.avaya.amsp.domain.PbxNumberLock;
import com.avaya.amsp.domain.PbxSpecialPort;
import com.avaya.amsp.domain.PbxSystem;
import com.avaya.amsp.masterdata.annotation.PBXTechAuditLog;
import com.avaya.amsp.masterdata.dtos.PbxNumberLockDto;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.PbxClusterRepository;
import com.avaya.amsp.masterdata.repo.PbxNumberLockRepository;
import com.avaya.amsp.masterdata.service.iface.PbxNumberLockServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PbxNumberLockService implements PbxNumberLockServiceIface {

	@Autowired
	private ModelMapper mapper;
	
	@Autowired
	PbxNumberLockRepository pbxNumberLockRepo;
	
	@Autowired
	PbxClusterRepository pbxClusterRepo;

	@PBXTechAuditLog(action = "update",entity = "PbxNumberLock",functionality = "PBX update existing Phone Number Lock")
	@Override
	public long savePbxNumberLock(PbxNumberLockDto pbxNumberLockDto) {
		// TODO Auto-generated method stub
		PbxCluster pbxcluster = null;
		log.info("Saving pbxNumberLock {}", pbxNumberLockDto);
		
		if(pbxNumberLockDto.getIdCluster() == null  || pbxNumberLockDto.getIdCluster() == 0) {
			throw new IllegalArgumentException(String.format("Cluster id can't be empty",pbxNumberLockDto.getIdCluster()));					
		}
		
		if(pbxNumberLockDto.getIdPbxCluster() != 0) {
			pbxcluster = pbxClusterRepo.getReferenceById(pbxNumberLockDto.getIdPbxCluster());	
			if(pbxcluster !=null ) {
				if(!pbxcluster.getClusterItem().getId().equals( pbxNumberLockDto.getIdCluster())) {
					throw new IllegalArgumentException(String.format("pbx cluster %s is not found in cluster %s ",pbxNumberLockDto.getIdPbxCluster(),pbxNumberLockDto.getIdCluster()));		
				}
			}			
		}else {
			pbxNumberLockDto.setIdPbxCluster(null);
		}

		PbxNumberLock pnl = mapper.map(pbxNumberLockDto, PbxNumberLock.class);
		ClusterItem cluster = new ClusterItem();
		cluster.setId(pbxNumberLockDto.getIdCluster());
		pnl.setClusterItem(cluster);
		pnl.setPbxcluster(pbxcluster);
		pnl.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
		
		PbxNumberLock savedPsp = pbxNumberLockRepo.save(pnl);
		return savedPsp.getId();
	}

	@PBXTechAuditLog(action = "update",entity = "PbxNumberLock",functionality = "PBX update existing Phone Number Lock")
	@Override
	public void updatePbxNumberLock(PbxNumberLockDto pbxNumberLockDto) {
		// TODO Auto-generated method stub
		log.info("updating pbxNumberLock {}", pbxNumberLockDto);
		
		Optional<PbxNumberLock> record = pbxNumberLockRepo.findById((long)pbxNumberLockDto.getId());
		record.ifPresentOrElse(value -> {
			
			PbxCluster pbxcluster = null;
			if(pbxNumberLockDto.getIdCluster() == null  || pbxNumberLockDto.getIdCluster() == 0) {
				throw new IllegalArgumentException(String.format("Cluster id can't be empty",pbxNumberLockDto.getIdCluster()));					
			}	
			if(pbxNumberLockDto.getIdPbxCluster() != 0) {
				pbxcluster = pbxClusterRepo.getReferenceById(pbxNumberLockDto.getIdPbxCluster());	
				if(pbxcluster != null ) {
					if(!pbxcluster.getClusterItem().getId().equals(pbxNumberLockDto.getIdCluster())) {
						throw new IllegalArgumentException(String.format("pbx cluster %s is not found in cluster %s ",pbxNumberLockDto.getIdPbxCluster(),pbxNumberLockDto.getIdCluster()));		
					}
				}			
			}else {
				pbxNumberLockDto.setIdPbxCluster(null);
			}
			
			ClusterItem cluster = new ClusterItem();
			cluster.setId(pbxNumberLockDto.getIdCluster());
			value.setClusterItem(cluster);

			value.setPbxcluster(pbxcluster);
			value.setPhoneNumber(pbxNumberLockDto.getPhoneNumber());
			value.setFreePbxSync(pbxNumberLockDto.isFreePbxSync());
			value.setReason(pbxNumberLockDto.getReason());
			value.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
			PbxNumberLock savedPsp = pbxNumberLockRepo.save(value);
			log.info("Number Lock record updated {} %s ", savedPsp);
			
		}, () -> {
				log.info("Number Lock record not found");
				throw new ResourceNotFoundException(String.format("Number lock record with Id %s not found ", pbxNumberLockDto.getId()));
		});
	}

	@PBXTechAuditLog(action = "delete",entity = "PbxNumberLock",functionality = "PBX delete Phone Number Lock")
	@Override
	public boolean deletePbxNumberLock(long idPbxNumberLock) {

		Optional<PbxNumberLock> exist = pbxNumberLockRepo.findById(idPbxNumberLock);
		if(exist.isPresent()) {
			pbxNumberLockRepo.deleteById(idPbxNumberLock);
			return true;
		}else {
			log.info("Number Lock record not found");
			throw new ResourceNotFoundException(String.format("Number lock record with Id %s not found ", idPbxNumberLock));
		}
	}

}
