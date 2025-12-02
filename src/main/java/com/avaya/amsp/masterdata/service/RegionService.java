package com.avaya.amsp.masterdata.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.ClusterRegion;
import com.avaya.amsp.domain.Region;
import com.avaya.amsp.masterdata.annotation.AuditLog;
import com.avaya.amsp.masterdata.dtos.ClusterDto;
import com.avaya.amsp.masterdata.dtos.RegionDto;
import com.avaya.amsp.masterdata.dtos.RegionToClusterDto;
import com.avaya.amsp.masterdata.exceptions.ResourceAlreadyExistsException;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.ClusterRegionRepository;
import com.avaya.amsp.masterdata.repo.ClusterRepository;
import com.avaya.amsp.masterdata.repo.RegionRepository;
import com.avaya.amsp.masterdata.service.iface.RegionServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
/**
 * 
 * @author yadav188 This class is used as a service implemenation of Region
 * 
 *
 */
public class RegionService implements RegionServiceIface {

	@Autowired
	private RegionRepository regionRepo;

	@Autowired
	private ClusterRepository clusterRepo;

	@Autowired
	private ClusterRegionRepository clusterRegionRepo;

	@Autowired
	ModelMapper mapper;

	/**
	 * This method is for getting the list of all Regions
	 */
	@Override
	public List<RegionDto> fetchAllRegions() {

		log.info("fetching available regions");
		List<RegionDto> regionDtoList = new ArrayList<RegionDto>();
		List<Region> regionData = regionRepo.findByActive(1L);
		if (regionData != null && !regionData.isEmpty()) {
			regionData.forEach(region -> {
				RegionDto regionDto = new RegionDto();
				regionDto.setId(region.getId());
				regionDto.setName(region.getName());
				regionDto.setActive(region.getActive());
				regionDto.setRemark(region.getDescription());
				regionDto.setLogCreatedBy(region.getLogCreatedBy());
				regionDto.setLogCreatedOn(region.getLogCreatedOn());
				regionDto.setLogUpdatedBy(region.getLogUpdatedBy());
				regionDtoList.add(regionDto);
			});
		} else {
			log.info("no regions found");
		}
		return regionDtoList;
	}

	/**
	 * This method is for getting the list of all clusters based on a Region id
	 */
	@Override
	public List<ClusterDto> fetchClustersByRegion(Long regionId) {

		log.info("fetching all clusters for region {}", regionId);
		List<ClusterDto> clusterDtoList = new ArrayList<ClusterDto>();
		Optional<Region> regionData = regionRepo.findById(regionId);
		regionData.ifPresentOrElse(clusterRegionData -> {
			Set<ClusterRegion> regionClusterData = clusterRegionData.getClusterRegion();
			regionClusterData.stream().forEach(clusterRegionConn -> {
				ClusterItem cluster = clusterRegionConn.getClusterItem();
				ClusterDto dto = mapper.map(cluster, ClusterDto.class);

				if (cluster.getActive() != 0) {
					dto.setAccCurrencyId(cluster.getAccCurrencyId().getId());
					dto.setLogCreatedBy(cluster.getLogCreatedBy());
					dto.setLogCreatedOn(cluster.getLogCreatedOn());
					dto.setLogUpdatedBy(cluster.getLogUpdatedBy());
					dto.setLogUpdatedOn(cluster.getLogUpdatedOn());
					clusterDtoList.add(dto);
				}
			});

		}, () -> {
			log.info("region with Id {} not found", regionId);
			throw new ResourceNotFoundException(String.format("region with Id %s not found", regionId));
		});
		return clusterDtoList;
	}

	/**
	 * This method is for creating a region
	 */
	@AuditLog(action = "Insert",entity = "Region",functionality = "Create New Region")
	@Override
	@org.springframework.transaction.annotation.Transactional
	public void createRegion(RegionDto regionDto) {

		if (fetchRegionByName(regionDto.getName()).isPresent()) {
			log.info("region already exists for given request {} ", regionDto.getName());
			throw new ResourceAlreadyExistsException(
					String.format("region with type %s is already exists", regionDto.getName()));
		}

		log.info("adding new region to database");
		//Region regionData = mapper.map(regionDto, Region.class);
		Region regionData = new Region();
		regionData.setActive(1);
		regionData.setName(regionDto.getName());
		regionData.setLogCreatedBy(regionDto.getLogCreatedBy());
		regionData.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
		regionData.setDescription(regionDto.getRemark());
		Region regionRecord = regionRepo.save(regionData);
		log.info("added record having id {}", regionRecord.getId());
	}

	/**
	 * This method is for updating a region
	 */
	@AuditLog(action = "Update",entity = "Region",functionality = "Update existing Region")
	@Override
	public void updateRegion(RegionDto regionDto) {
		log.info("updating region record with ID {}", regionDto.getId());

		Optional<Region> record = regionRepo.findById(regionDto.getId());
		record.ifPresentOrElse(value -> {
			value.setName(regionDto.getName());
			value.setDescription(regionDto.getRemark());
			value.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
			value.setLogUpdatedBy(regionDto.getLogUpdatedBy());
			regionRepo.save(value);
		}, () -> {
			log.info("Region record not found");
			throw new ResourceNotFoundException(String.format("Region with Id %s not found ", regionDto.getId()));
		});
	}

	/**
	 * This method is for deleting a Region
	 */
	@AuditLog(action = "delete",entity = "Region",functionality = "delete existing Region")
	@Override
	public void deleteRegion(Long regionId) {

		log.info("Removing Region record with ID {}", regionId);
		Optional<Region> record = regionRepo.findById(regionId);
		record.ifPresentOrElse(value -> {
			value.setActive(0);
			regionRepo.save(value);
		}, () -> {
			log.info("Region record not found");
			throw new ResourceNotFoundException(String.format("Region with Id %s not found ", regionId));
		});

	}

	@Override
	public Optional<Region> fetchRegionByName(String name) {

		log.info("Fetching region with name {}", name);
		Region regionName = regionRepo.findByName(name);
		return Optional.ofNullable(regionName);
	}

	@Override
	public void addClustersToAllocation(Long regionId, RegionToClusterDto clusters) {

		log.info("request for adding clusters allocation");

		Optional<Region> record = regionRepo.findById(regionId);
		record.ifPresentOrElse(region -> {
			List<ClusterItem> clusterRecords = clusterRepo.findAllById(clusters.getClusterIds());
			log.info("no of rows {}", clusterRecords.size());
			if (clusterRecords.size() != clusters.getClusterIds().size()) {
				throw new IllegalArgumentException("Some of clusters not found");
			}

			List<ClusterRegion> clusterRegionRecords = new ArrayList<ClusterRegion>();

			clusterRecords.forEach((clusterConn) -> {
				
		        boolean isAssignedToAnotherRegion = clusterRegionRepo.existsByClusterItemAndRegionNot(clusterConn, region);

		        if (isAssignedToAnotherRegion) {
		            log.info("Cluster {} is already assigned to another region, skipping", clusterConn.getId());
		            return;  // Skip this cluster and move to the next one
		        }
				
				ClusterRegion clusterReg = new ClusterRegion();
				clusterReg.setClusterItem(clusterConn);
				clusterReg.setRegion(region);
				clusterRegionRecords.add(clusterReg);
			});

			clusterRegionRepo.addClustersToRegion(clusterRegionRecords);

		}, () -> {
			log.info("regionId not found");
			throw new ResourceNotFoundException(String.format("region with Id %s not found ", regionId));
		});

	}

	@AuditLog(action = "delete",entity = "ClusterRegion",functionality = "delete Clusters From Region")
	@Override
	public void deleteClustersFromAllocation(Long regionId, RegionToClusterDto clusters) {

		Optional<Region> regionRecord = regionRepo.findById(regionId);

		regionRecord.ifPresentOrElse(record -> {

			Set<ClusterRegion> clusterRegionRecord = record.getClusterRegion();

			if (clusterRegionRecord != null && clusterRegionRecord.size() != 0) {

				List<ClusterRegion> clusterRegionList = new ArrayList<ClusterRegion>();
				clusters.getClusterIds().forEach(clusterId -> {

					ClusterRegion clusterRegion = new ClusterRegion();
					clusterRegion.setClusterItemId(clusterId);
					clusterRegion.setRegionId(regionId);

					ClusterRegion clusterFromdb = clusterRegionRecord.stream()
							.filter(value -> value.equals(clusterRegion)).findFirst()
							.orElseThrow(() -> new IllegalArgumentException(String
									.format("Clusters with Id %s not found for region Id %s", clusterId, regionId)));

					clusterRegionList.add(clusterFromdb);

				});

				log.info("Removing clusters from region {} ", clusterRegionList);
				clusterRegionRepo.deleteAllInBatch(clusterRegionList);

			} else {
				throw new IllegalArgumentException(
						String.format("No cluster is associated with region with Id %s", regionId));
			}

		}, () -> {
			log.info("region with Id {} not found", regionId);
			throw new ResourceNotFoundException(String.format("region with Id %s not found", regionId));
		});

	}

	@Override
	public List<ClusterDto> fetchClustersByRegionAndClusterIds(Long regionId, List<Long> clusterIds) {
		log.info("fetching all clusters for region {}", regionId);
		List<ClusterDto> clusterDtoList = new ArrayList<ClusterDto>();
		Optional<Region> regionData = regionRepo.findById(regionId);
		regionData.ifPresentOrElse(clusterRegionData -> {
			Set<ClusterRegion> regionClusterData = clusterRegionData.getClusterRegion();
			regionClusterData.stream().forEach(clusterRegionConn -> {
				ClusterItem cluster = clusterRegionConn.getClusterItem();
				boolean isPresent = clusterIds.stream().anyMatch(id -> cluster.getId().equals(id));
				if (isPresent) {
					ClusterDto dto = mapper.map(cluster, ClusterDto.class);

					if (cluster.getActive() != 0) {
						dto.setAccCurrencyId(cluster.getAccCurrencyId().getId());
						dto.setLogCreatedBy(cluster.getLogCreatedBy());
						dto.setLogCreatedOn(cluster.getLogCreatedOn());
						dto.setLogUpdatedBy(cluster.getLogUpdatedBy());
						dto.setLogUpdatedOn(cluster.getLogUpdatedOn());
						clusterDtoList.add(dto);
					}
				}
			});

		}, () -> {
			log.info("region with Id {} not found", regionId);
			throw new ResourceNotFoundException(String.format("region with Id %s not found", regionId));
		});
		return clusterDtoList;
	}
	
}
