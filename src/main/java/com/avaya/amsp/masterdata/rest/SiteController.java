package com.avaya.amsp.masterdata.rest;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.avaya.amsp.domain.TemplateConfiguration;
import com.avaya.amsp.masterdata.dtos.*;
import com.avaya.amsp.masterdata.service.iface.TemplateConfigurationServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.service.iface.SiteServiceIface;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
/**
 * 
 * @author yadav188 
 * This class work as a controller for Site Entity
 */
public class SiteController {

	@Autowired
	SiteServiceIface siteService;

	/**
	 * This method is used to get all active sites
	 * 
	 * @return : 200 OK
	 */

	@GetMapping("/v1/sites")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchSites() {

		log.info("requesting for getting all available sites");
		List<SiteDto> response = new ArrayList<>();
		response = siteService.fetchAllSites();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * This method returns all the available sites for a cluster id
	 * 
	 * @param id : cluster id
	 * @return : 200 OK
	 */

	@GetMapping("/v1/clusters/{id}/sites")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchSitesByCluster(@PathVariable Long id) {

		log.info("requesting for getting all available sites for cluster");
		List<SiteDto> response = new ArrayList<>();
		response = siteService.fetchSitesByCluster(id);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * This method will be used to create a site
	 * 
	 * @param request : request data from UI/controller
	 * @return : 201 OK
	 */

	@PostMapping("/v1/sites")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> persistSite(@Valid @RequestBody SiteDto request, @AuthenticationPrincipal AMSPUser amspUser) {

		log.info("request received to create a new site {}", request);
		request.setLogUpdatedBy(amspUser.getUsername());
		siteService.createSite(request);
		return ResponseEntity.status(HttpStatus.CREATED).body("");

	}
	
	// Site Pool Mapping Start...
	
	/**
	 * This method add the selected pools to the selected site
	 * @param siteId  -- site id from request
	 * @param pools - List of pool ids
	 * @return
	 */
	@PostMapping("/v1/sites/{id}/pools")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> addPoolsToSite(@PathVariable("id") Long siteId,@RequestBody @Valid SiteToPoolDto pools) {
		log.info("request received to add pools to sites {} for pool {}",siteId,pools);		
		siteService.addAssignPools(siteId, pools);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	/**
	 * This method will be used to update a site
	 * 
	 * @param request : request data from UI
	 * @param siteId  : selected site id
	 * @return : 200 OK
	 */
	@PutMapping("/v1/sites/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> updateSites(@RequestBody SiteDto request, @PathVariable("id") Long siteId, @AuthenticationPrincipal AMSPUser amspUser) {

		request.setId(siteId);
		request.setLogUpdatedBy(amspUser.getUsername());
		log.info("request received to update site {}", request);
		siteService.updateSite(request);
		return ResponseEntity.status(HttpStatus.OK).body("");

	}

	/**
	 * This method will be used to delete a site
	 * 
	 * @param siteId : selected site id from UI
	 * @return : 200 OK
	 */

	@DeleteMapping("/v1/sites/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deleteSites(@PathVariable("id") Long siteId) {

		log.info("request received to delete site {}", siteId);
		siteService.deleteSite(siteId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}
	
	/**
	 * This method will delete the pools from assigned sites
	 * @param siteId  : site id
	 * @param pools : list of all pools
	 * @return
	 */
	@DeleteMapping("/v1/sites/{id}/pools")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deletePoolFromSite(@PathVariable("id") Long siteId,@RequestBody @Valid SiteToPoolDto pools) {

		log.info("request received to delete pools from site {}", siteId);
		siteService.deleteAssignPools(siteId, pools);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}
	
	/**
	 * This method fetches the pools for a site given a site id
	 * @param siteId  : site id
	 * @param pools : list of all pools
	 * @return
	 */
	@GetMapping("/v1/sites/{id}/pools")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getPoolsForSite(@PathVariable("id") Long siteId) {

		log.info("request received to get pools for site {}",  siteId);
		Set<PoolDto> sitePoolsDto = siteService.getPoolsForSite(siteId);
		return ResponseEntity.status(HttpStatus.OK).body(sitePoolsDto);
	}	

	@GetMapping("/v1/sites/{id}/contractFls")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public List<ContractFlDto> getContractFlsForSite(@PathVariable("id") Long siteId) {

		log.info("request received to get contractFls for site {}",  siteId);
		List<ContractFlDto> siteContractFls = siteService.getContractFlsForSite(siteId);
		return siteContractFls;
	}	
	
	@PostMapping("/v1/sites/{id}/contractFls")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public boolean addContractFlsForSite(@PathVariable("id") Long siteId, @RequestBody List<ContractFlDto> contractFlDto, @AuthenticationPrincipal AMSPUser amspUser) {

		log.info("Adding contractFl {} for site {}",  contractFlDto, siteId);
		boolean saved = siteService.addContractFlsForSite(siteId,contractFlDto, amspUser.getUsername());
		return saved;
	}	

	@DeleteMapping("/v1/sites/{id}/contractFls")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public boolean deleteContractFlsForSite(@PathVariable("id") Long siteId, @RequestBody List<Long> contractFls,@AuthenticationPrincipal AMSPUser amspUser) {

		log.info("Deleting contractFl {} for site {}",  contractFls, siteId);
		boolean saved = siteService.deleteContractFlsForSite(siteId,contractFls, amspUser.getUsername());
		return saved;
	}	
	
	@GetMapping("/v1/sites/{id}/shippingaddresses")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public List<ShippingAddressDto> getShippingAddressForSite(@PathVariable("id") Long siteId) {

		log.info("request received to get shippingAddress for site {}",  siteId);
		List<ShippingAddressDto> shippingAddress = siteService.getShippingAddressForSite(siteId);
		return shippingAddress;
	}
	
	/**
	 * This method fetches the articles for a specific site Id and pool Id
	 * 
	 * @param siteId : Pool id
	 * @param pools  : list of all associated articles
	 * @return
	 */
	@GetMapping("/v1/sites/{siteId}/pool/{poolId}/articles")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getArticlesForPool(@AuthenticationPrincipal AMSPUser amspUser,@PathVariable("siteId") Long siteId, @PathVariable("poolId") Long poolId) {
		log.info("request received to get aticles for pool {}", poolId);
		String langId=amspUser.getDefaultLanguageId();
		List<ArticlePropertiesDto> articledata = siteService.getArticlesForSitePool(langId,siteId, poolId);
		return ResponseEntity.status(HttpStatus.OK).body(articledata);
	}



}
