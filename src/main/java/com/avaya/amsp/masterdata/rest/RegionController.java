package com.avaya.amsp.masterdata.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.domain.enums.RoleGroup;
import com.avaya.amsp.masterdata.dtos.ClusterDto;
import com.avaya.amsp.masterdata.dtos.RegionDto;
import com.avaya.amsp.masterdata.dtos.RegionToClusterDto;
import com.avaya.amsp.masterdata.service.RegionService;
import com.avaya.amsp.security.config.AmspGrantedAuthority;
import com.avaya.amsp.security.user.AMSPUser;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author yadav188 
 * This class is work as controller for Region
 *
 */
@RestController
@Slf4j
public class RegionController {

	@Autowired
	RegionService regionService;

	/**
	 * This method will be used for getting all the available active regions
	 * 
	 * @return : 200 OK
	 */
	@GetMapping("/v1/regions")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchRegions() {

		log.info("requesting for getting all available regions");
		List<RegionDto> response = new ArrayList<>();
		response = regionService.fetchAllRegions();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * This method will fetch all the available clusters on the basis of regionId
	 * passes from UI
	 * 
	 * @param regionId : The selected RegionId from UI
	 * @return : 200 OK
	 */
	@GetMapping("/v1/regions/{id}/clusters")
	@PreAuthorize("hasAnyRole('TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchClustersByRegion(@PathVariable("id") Long regionId, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("requesting for getting all available clusters by region");
		List<ClusterDto> response = new ArrayList<>();
		Collection<GrantedAuthority> authorities = amspUser.getAuthorities();
		Iterator<GrantedAuthority> itr = authorities.iterator();
		AmspGrantedAuthority tkPGrantedAuthority = null;
		while(itr.hasNext()) {
			GrantedAuthority authority = itr.next();
			if(authority.getAuthority().contains(RoleGroup.TK_P.name())) {
				if(authority instanceof AmspGrantedAuthority) {
					tkPGrantedAuthority = (AmspGrantedAuthority) authority;
				}
			}
			else {
				response = regionService.fetchClustersByRegion(regionId);
				return ResponseEntity.status(HttpStatus.OK).body(response);
			}
		}
		if (tkPGrantedAuthority != null) {
			List<Long> clusterIds = tkPGrantedAuthority.getClusterIdList();
			response = regionService.fetchClustersByRegionAndClusterIds(regionId, clusterIds);
		}
		else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * This method will be used to create a region
	 * 
	 * @param request : request data passed from UI/controller
	 * @return : 201
	 */

	@PostMapping("/v1/regions")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> persistRegions(@Valid @RequestBody RegionDto request, @AuthenticationPrincipal AMSPUser amspUser) {

		log.info("request received to create a new region{}", request);
		request.setLogCreatedBy(amspUser.getUsername());
		regionService.createRegion(request);
		return ResponseEntity.status(HttpStatus.CREATED).body("");

	}
	
	/**
	 * This method will be used for adding the cluster to a region from list of others clusters
	 * @param regionId  : Region id
	 * @param clusters : List of cluster ids needs to be added
	 * @return
	 */
		
	@PostMapping("/v1/regions/{id}/clusters")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> addClusterToRegion(@PathVariable("id") Long regionId,@RequestBody @Valid RegionToClusterDto clusters) {
		log.info("request received to add clusters to regionId {}. cluster {}",regionId,clusters);		
		regionService.addClustersToAllocation(regionId, clusters);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	/**
	 * This method will be used to update the existing region
	 * 
	 * @param request  : request parameter passed from UI
	 * @param regionId : region id which we need to update the data
	 * @return: 200 OK
	 */
	@PutMapping("/v1/regions/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> updateRegions(@RequestBody RegionDto request, @PathVariable("id") Long regionId, @AuthenticationPrincipal AMSPUser amspUser) {

		request.setId(regionId);
		request.setLogUpdatedBy(amspUser.getUsername());
		log.info("request received to update region {}", request);
		regionService.updateRegion(request);
		return ResponseEntity.status(HttpStatus.OK).body("");

	}

	/**
	 * This method will be used to delete a region
	 * 
	 * @param regionId : Id for which region needs to deleted
	 * @return : 200 OK
	 */

	@DeleteMapping("/v1/regions/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deleteRegions(@PathVariable("id") Long regionId) {

		log.info("request received to delete region {}", regionId);
		regionService.deleteRegion(regionId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}
	
	/**
	 * This method will be used to delete clusters from region
	 * 
	 * @param regionId : Id for which associated clusters needs to be deleted
	 * @return : 200 OK
	 */
	
	@DeleteMapping("/v1/regions/{id}/clusters")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deleteClustersFromRegion(@PathVariable("id") Long regionId, @RequestBody @Valid RegionToClusterDto clusters) {

		log.info("request received to delete clusters from region {}", regionId);
		regionService.deleteClustersFromAllocation(regionId, clusters);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}

}