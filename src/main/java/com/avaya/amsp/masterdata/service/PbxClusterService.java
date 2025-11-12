package com.avaya.amsp.masterdata.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.Country;
import com.avaya.amsp.domain.PbxCluster;
import com.avaya.amsp.domain.PbxNumberLock;
import com.avaya.amsp.domain.PbxNumberRange;
import com.avaya.amsp.domain.PbxPhoneNumberType;
import com.avaya.amsp.domain.PbxSystem;
import com.avaya.amsp.masterdata.annotation.PBXTechAuditLog;
import com.avaya.amsp.masterdata.dtos.PbxClusterDto;
import com.avaya.amsp.masterdata.dtos.PbxNumberLockDto;
import com.avaya.amsp.masterdata.dtos.PbxNumberRangeDto;
import com.avaya.amsp.masterdata.dtos.PbxSystemDto;
import com.avaya.amsp.masterdata.dtos.PbxWrapper;
import com.avaya.amsp.masterdata.dtos.PhysicalPbxDto;
import com.avaya.amsp.masterdata.dtos.SiteDto;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.ClusterRepository;
import com.avaya.amsp.masterdata.repo.CountryRepository;
import com.avaya.amsp.masterdata.repo.PbxClusterRepository;
import com.avaya.amsp.masterdata.repo.PbxNumberLockRepository;
import com.avaya.amsp.masterdata.repo.PbxNumberRangeRepository;
import com.avaya.amsp.masterdata.repo.PbxSystemRepository;
import com.avaya.amsp.masterdata.repo.PbxSystemSiteRepository;
import com.avaya.amsp.masterdata.service.iface.PbxClusterServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PbxClusterService implements PbxClusterServiceIface {

	@Autowired
	private PbxClusterRepository pbxClusterRepo;

	@Autowired
	private PbxNumberLockRepository pbxNumLockRepo;

	@Autowired
	private PbxSystemRepository pbxSystemRepo;

	@Autowired
	private PbxNumberRangeRepository pbxNumberRangeRepo;

	@Autowired
	private ClusterRepository clusterRepo;
	@Autowired
	private CountryRepository countryRepo;
	@Autowired
	private ModelMapper mapper;

	@Autowired
	PbxSystemSiteRepository pbxSystemSiteRepository;

	@Override
	public List<PbxClusterDto> fetchAllPbxClusters() {
		log.debug("Fetching all pbxClusters");
		List<PbxCluster> pbxClusterList = pbxClusterRepo.findAll();
		List<PbxClusterDto> pbxClusterDtos = new ArrayList<>();

		for (PbxCluster pbxCluster : pbxClusterList) {
			if (pbxCluster.getActive() != 0) {
				pbxClusterDtos.add(mapper.map(pbxCluster, PbxClusterDto.class));
			}
		}
		log.debug("Returning pbxClusterDtos as {}", pbxClusterDtos);

		return pbxClusterDtos;
	}

	@Override
	@PBXTechAuditLog(action = "update",entity = "PbxCluster",functionality = "PBX update existing PbxCluster")
	@org.springframework.transaction.annotation.Transactional
	public long savePbxCluster(PbxClusterDto pbxClusterDto) {
		//PbxCluster pbxCluster = mapper.map(pbxClusterDto, PbxCluster.class);
		PbxCluster pbxCluster = new PbxCluster();
		pbxCluster.setName(pbxClusterDto.getName());
		pbxCluster.setAreacode(pbxClusterDto.getAreacode());
		pbxCluster.setAreacodeInfo(pbxClusterDto.getAreacodeInfo());
		ClusterItem clusterItem = clusterRepo.findById(pbxClusterDto.getClusterItem().getId())
			    .orElseThrow(() -> new ResourceNotFoundException("ClusterItem not found"));
		pbxCluster.setClusterItem(clusterItem);
		Country country = countryRepo.findById(pbxClusterDto.getCountry().getId())
			    .orElseThrow(() -> new ResourceNotFoundException("ClusterItem not found"));
		pbxCluster.setCountry(country);
		pbxCluster.setDescriptionEnglish(pbxClusterDto.getDescriptionEnglish());
		pbxCluster.setPbxId(pbxClusterDto.getPbxId());
		pbxCluster.setLogCreatedBy(pbxClusterDto.getLogCreatedBy());
		pbxCluster.setLogCreatedOn(LocalDateTime.now());
		pbxCluster.setActive(1);
		PbxCluster savedPbxCluster = pbxClusterRepo.save(pbxCluster);
		return savedPbxCluster.getId();
	}

	@Override
	@PBXTechAuditLog(action = "update",entity = "PbxCluster",functionality = "PBX update existing PbxCluster")
	public PbxClusterDto updatePbxCluster(PbxClusterDto pbxClusterDto) {
		PbxCluster pbxCluster = pbxClusterRepo.getReferenceById(pbxClusterDto.getId());
		pbxCluster.setClusterItem(clusterRepo.getReferenceById(pbxClusterDto.getClusterItem().getId()));
		pbxCluster.setCountry(countryRepo.getReferenceById(pbxClusterDto.getCountry().getId()));
		BeanUtils.copyProperties(pbxClusterDto, pbxCluster, new String[] { "logCreatedOn", "logCreatedBy" });
		pbxCluster.setLogUpdatedOn(LocalDateTime.now());
		log.info("Updating pbxCluster as {}", pbxCluster);
		pbxCluster = pbxClusterRepo.save(pbxCluster);
		return mapper.map(pbxCluster, PbxClusterDto.class);
	}

	@Override
	public List<PbxNumberLockDto> fetchAllNumberLockByPbxCluser(Long idPbxCluster) {

		log.info("fetching numberLock for pbx cluster {}", idPbxCluster);

		List<PbxNumberLockDto> dtos = new ArrayList<PbxNumberLockDto>();
		List<PbxNumberLock> record = pbxNumLockRepo.findBypbxCluster_Id(idPbxCluster);

		if (record != null && !record.isEmpty()) {
			record.forEach(value -> {
				PbxNumberLockDto pbxNumberLockDto = mapper.map(value, PbxNumberLockDto.class);
				pbxNumberLockDto.setIdPbxCluster(idPbxCluster);
				dtos.add(pbxNumberLockDto);
			});
		} else {
			log.info("no pbx cluster found");
			throw new ResourceNotFoundException(String.format("cluster with Id %s not found ", idPbxCluster));
		}
		return dtos;
	}

	@Override
	public List<PbxSystemDto> fetchAllPbxSystemByPbxCluster(Long idPbxCluster) {
		log.info("fetching pbxSystems for pbx cluster {}", idPbxCluster);

		    List<PbxSystem> records = pbxSystemRepo.findByPbxCluster_Id(idPbxCluster);

		    if (records == null || records.isEmpty()) {
		        log.info("No PBX Systems found for cluster {}", idPbxCluster);
		        throw new ResourceNotFoundException(String.format("PBX cluster with ID %s not found", idPbxCluster));
		    }

		    return records.stream().map(system -> {
		        PbxSystemDto dto = mapper.map(system, PbxSystemDto.class);
		        if (system.getpbxSystemSite() != null) {
		            List<SiteDto> sites = system.getpbxSystemSite().stream()
		                .map(site -> mapper.map(site.getSite(), SiteDto.class))
		                .collect(Collectors.toList());
		            dto.setSites(sites);
		        }
		        return dto;
		    }).collect(Collectors.toList());
	}

	@PBXTechAuditLog(action = "delete",entity = "PbxCluster",functionality = "PBX delete PbxCluster")
	@Override
	public void deletePbxClusters(Long pbxClusterId) {
		log.info("deleting pbx cluster record with ID {}", pbxClusterId);
		Optional<PbxCluster> record = pbxClusterRepo.findById(pbxClusterId);
		record.ifPresentOrElse(value -> {
			value.setActive(0);
			pbxClusterRepo.save(value);
		}, () -> {
			log.info("pbx cluster record not found");
			throw new ResourceNotFoundException(String.format("pbx cluster with Id %s not found ", pbxClusterId));
		});

	}

	@Override
	public List<PbxNumberRangeDto> fetchNumberRangeByPbxCluster(int idPbxCluster) {
		log.info("requesting for fetching pbx number range by pbxcluster");

		List<PbxNumberRangeDto> dtos = new ArrayList<PbxNumberRangeDto>();
		List<PbxNumberRange> record = pbxNumberRangeRepo.findByIdPbxCluster(idPbxCluster);

		if (record != null && !record.isEmpty()) {

			record.forEach(value -> {

				if (value.getActive() != 0) {

					PbxNumberRangeDto pbxNumberRangeDto = mapper.map(value, PbxNumberRangeDto.class);
					pbxNumberRangeDto.setPbxClusterId(idPbxCluster);
					pbxNumberRangeDto.setPbxId(value.getIdPbxSystem());
					PbxPhoneNumberType phoneType = new PbxPhoneNumberType();
					phoneType.setId(value.getPhoneNumberType().getId());
					pbxNumberRangeDto.setPhoneNumType(phoneType.getId());
					dtos.add(pbxNumberRangeDto);
				}
			});
		} else {
			log.info("no pbx number range found");
			throw new ResourceNotFoundException(String.format("pbxcluster with Id %s not found ", idPbxCluster));
		}
		return dtos;
	}

	@Override
	public List<PhysicalPbxDto> fetchAllPhysicalPbxbyPbxCluster(Long idPbxCluster) {

		List<PhysicalPbxDto> dtos = new ArrayList<PhysicalPbxDto>();
		List<String> record = pbxSystemRepo.findPhysicalPbxByPbxCluster_Id(idPbxCluster);

		if (record != null && !record.isEmpty()) {
			record.forEach(value -> {
				PhysicalPbxDto dto = mapper.map(value, PhysicalPbxDto.class);
				dto.setPhysicalPbx(value);
				dtos.add(dto);

			});
		} else {
			log.info("no pbx cluster found");
			throw new ResourceNotFoundException(String.format("pbx cluster with Id %s not found ", idPbxCluster));
		}
		return dtos;
	}

	@Override
	public PbxWrapper fetchPbxByAreaCode(String areaCode) {
		List<PbxSystemDto> pbxSystemDtos = new ArrayList<PbxSystemDto>();

		PbxCluster pbxCluster = pbxClusterRepo.findFirstByAreacodeAndActiveTrue(areaCode)
		.orElseThrow( () -> {
			log.info("Pbx cluster not found for areaCode {}",areaCode);
			throw new ResourceNotFoundException(String.format("Pbx cluster not found for areaCode %s ", areaCode));
		});

		List< PbxSystem > pbxSystems = pbxSystemRepo.fetchByPbxClusterId(pbxCluster.getId());

		//if there is no pbx sub system , validation will fail here , no need to check site
		List< SiteDto > sites = new ArrayList<>();
		if(pbxSystems.size()>0) {

			List< Object[] > sitesResult = pbxSystemRepo.fetchPbxSystemSites(areaCode);
			sitesResult.stream().forEach(rcd->{
				SiteDto dto = new SiteDto();
				dto.setId(Long.valueOf((Integer)rcd[0]));
				dto.setName((String)rcd[1]);
				sites.add(dto);
			});

			pbxSystems.stream().forEach(pbxSystem->{
				PbxSystemDto pbxSystemDto = new PbxSystemDto();
				pbxSystemDto.setId(pbxSystem.getId());
				pbxSystemDto.setSfbsSystem(pbxSystem.isSfbsSystem());
				pbxSystemDto.setTeamsSystem(pbxSystem.isTeamsSystem());

				pbxSystemDtos.add(pbxSystemDto);
			});

		}else{
			log.info("No pbx system found");
		}

		PbxWrapper wrapper = new PbxWrapper();
		wrapper.setClusterId(pbxCluster.getClusterItem().getId());
		wrapper.setPbxSystemCount(pbxSystems.size());
		wrapper.setPbxSystemDtos(pbxSystemDtos);
		wrapper.setSites(sites);
		return wrapper;

	}

}
