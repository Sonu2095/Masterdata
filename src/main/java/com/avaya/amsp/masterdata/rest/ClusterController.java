package com.avaya.amsp.masterdata.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.avaya.amsp.domain.enums.RoleGroup;
import com.avaya.amsp.masterdata.dtos.*;
import com.avaya.amsp.masterdata.service.iface.TemplateConfigurationServiceIface;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.service.ArticleClusterService;
import com.avaya.amsp.masterdata.service.ClusterService;
import com.avaya.amsp.security.config.AmspGrantedAuthority;
import com.avaya.amsp.security.user.AMSPUser;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
/**
 * 
 * @author yadav188 This is working as controller for Cluster
 *
 */
@RequestMapping("/v1/clusters")
public class ClusterController {

	@Autowired
	ClusterService clusterService;

	@Autowired
	ArticleClusterService articleClusterService;

	@Autowired
	TemplateConfigurationServiceIface templateConfigurationService;

	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchClusters(@AuthenticationPrincipal AMSPUser amspUser) {

		log.info("requesting for getting all available clusters");
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
				response = clusterService.fetchAllClusters();
				return ResponseEntity.status(HttpStatus.OK).body(response);
			}
		}
		if (tkPGrantedAuthority != null) {
			List<Long> clusterIds = tkPGrantedAuthority.getClusterIdList();
			response = clusterService.fetchClustersByClusterIds(clusterIds);
		}
		else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PostMapping("")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> persistClusters(@Valid @RequestBody ClusterDto request, @AuthenticationPrincipal AMSPUser amspUser) {

		log.info("request received to create a new cluster{}", request);
		request.setLogCreatedBy(amspUser.getUsername());
		clusterService.createCluster(request);
		return ResponseEntity.status(HttpStatus.CREATED).body("");

	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> updateClusters(@RequestBody ClusterDto request, @PathVariable("id") Long clusterId , @AuthenticationPrincipal AMSPUser amspUser) {

		request.setId(clusterId);
		log.info("request received to update cluster {}", request);
		request.setLogCreatedBy(amspUser.getUsername());
		clusterService.updateCluster(request);
		return ResponseEntity.status(HttpStatus.OK).body("");

	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deleteClusters(@PathVariable("id") Long clusterId) {

		log.info("request received to delete cluster {}", clusterId);
		clusterService.deleteCluster(clusterId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}

	@GetMapping("/{id}/connections")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchConnectionByCluster(@PathVariable("id") Long clusterId) {

		log.info("request received to fetch all connections for cluster {}", clusterId);
		List<ConnectionDto> response = new ArrayList<>();
		response = clusterService.fetchConnectionsByCluster(clusterId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * This is for getting all the articles based on the cluster Id
	 * 
	 * @param clusterId
	 * @return:
	 */
	@GetMapping("/{id}/articles")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchArticlesByCluster(@PathVariable("id") Long clusterId) {

		log.info("request received to fetch all articles for cluster {}", clusterId);
		List<ArticleToClusterDto> response = new ArrayList<>();
		response = clusterService.fetchArticlesByCluster(clusterId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	/**
	 * This is for getting all the articles based on the cluster Id && master partstatus
	 * 
	 * @param clusterId
	 * @return:
	 */
	@GetMapping("/{id}/articles/partlist")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchArticlesByClusterForPartList(@PathVariable("id") Long clusterId) {

		log.info("request received to fetch all articles for cluster and enabled master partstatus{}", clusterId);
		List<ArticleToClusterDto> response = new ArrayList<>();
		response = clusterService.fetchArticlesByClusterForPartList(clusterId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * This is for getting all the articles based on the cluster Id
	 * 
	 * @param clusterId
	 * @return:
	 */
	@GetMapping("/{id}/poolhandling/articles")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchArticlesByClusterPoolEnabled(@PathVariable("id") Long clusterId) {

		log.info("request received to fetch all articles for cluster {}", clusterId);
		List<ArticleToClusterDto> response = new ArrayList<>();
		response = clusterService.fetchArticlesByClusterPoolEnabled(clusterId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	/**
	 * This is for getting all the articles based on the cluster Id eligible to be added to the pool. (HW type)
	 * 
	 * @param clusterId
	 * @return:
	 */
	@GetMapping("/{id}/pool/articles")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchArticlesByClusterForPool(@PathVariable("id") Long clusterId) {

		log.info("request received to fetch all articles by cluster id for pool  {}", clusterId);
		List<ArticleToClusterDto> response = new ArrayList<>();
		response = clusterService.fetchArticlesByClusterForPool(clusterId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * This is for adding/assigning articles to cluster
	 * 
	 * @param clusterId
	 * @param articles
	 * @return
	 */

	@PostMapping("/{id}/articles")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> addArticlesToCluster(@PathVariable("id") Long clusterId,
			@RequestBody @Valid ArticleClusterDetailDto articles, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to add articles {} to clusters {} ", articles, clusterId);
		articles.setUser(amspUser.getUsername());
		clusterService.addArticlesToCluster(clusterId, articles);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	/**
	 * This is for deleting articles from cluster
	 * 
	 * @param clusterId
	 * @param articles
	 * @return
	 */
	@DeleteMapping("/{id}/articles")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deleteArticlesFromcluster(@PathVariable("id") Long clusterId,
			@RequestBody @Valid ArticleClusterDetailDto articles, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to delete clusters from article {}", clusterId);
		articles.setUser(amspUser.getUsername());
		clusterService.deleteArticlesFromCluster(clusterId, articles);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}

	@GetMapping("/{id}/pbxclusters")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchPbxByCluster(@PathVariable("id") Long id) {
		log.info("requesting for getting all pbxclusters by cluster id");
		List<PbxClusterDto> response = clusterService.fetchPbxByCluster(id);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/{id}/numberlocks")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchAllNumberLockByCluser(@PathVariable("id") long idCluster) {

		log.info("requesting for getting all number locks by cluser {}", idCluster);
		List<PbxNumberLockDto> response = clusterService.fetchAllNumberLockByCluser(idCluster);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/{id}/pbxsystems")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchAllPbxSystemsByCluster(@PathVariable("id") long idCluster) {
		log.info("requesting for getting all pbx-systems by Cluster {}", idCluster);
		List<PbxSystemDto> response = clusterService.fetchAllPbxSystemByCluster(idCluster);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/*
	 * to get the cluster partlist on the basis of cluster id and master part status
	 */

	@GetMapping("/clusters/{clusterId}/{partStatus}/leadarticles")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchArticlesByParts(@PathVariable("clusterId") Long clusterId,
			@PathVariable("partStatus") int partStatus, @AuthenticationPrincipal AMSPUser user) {
		List<ArticleClusterPartListDto> response = new ArrayList<>();
		log.info("request received to fetch article cluster from database for cluster partlist enabled/disabled");
		response = articleClusterService.fetchAllArticleClusterByParts(clusterId, partStatus, user);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@GetMapping("/{clusterId}/pbxid")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchPbxIdByCluster(@PathVariable("clusterId") Long clusterId){
		List<PbxIdClusterDto> response = new ArrayList<>();
		log.info("request received to fetch article cluster from database for cluster partlist enabled/disabled");
		response = clusterService.fetchAllPbxIdByCluster(clusterId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
		
	}
	
	@GetMapping("/{clusterId}/areacode")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchAreaCodeByCluster(@PathVariable("clusterId") Long clusterId){
		List<Map<String, String>> response = new ArrayList<>();
		log.info("request received to fetch article cluster from database for cluster partlist enabled/disabled");
		response = clusterService.fetchAllAreaCodeByCluster(clusterId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
		
	}	
	/*
	@GetMapping("/partlist/{clusterId}/{partStatus}/leadarticles")
	public ResponseEntity<Object> fetchArticlesByParts(@PathVariable("clusterId") Long clusterId,
			@PathVariable("partStatus") int partStatus) {
		List<ArticleClusterPartListDto> response = new ArrayList<>();
		log.info("request received to fetch article cluster from database for cluster partlist enabled/disabled");
		response = articleClusterService.fetchAllArticleClusterByParts(clusterId, partStatus);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}*/


	@GetMapping("/{clusterId}/template-configs")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public List< TemplateConfigurationDTO > fetchTemplateConfigurationsByCluster(@PathVariable("clusterId") Long clusterId) {
		log.info("request received to get template configurations for cluster {}", clusterId);
		List<TemplateConfigurationDTO> templateConfigurations = templateConfigurationService.fetchByClusterId(clusterId);
		return templateConfigurations;
	}


	@GetMapping("/{clusterId}/connections/{connectionId}/template-configs")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public List< TemplateConfigurationDTO > fetchTemplateConfigurationsByClusterAndConnection(@PathVariable("clusterId") Long clusterId,@PathVariable("connectionId") Long connectionId) {
		log.info("request received to get template configurations for cluster and connection  {}-{}", clusterId,connectionId);
		List<TemplateConfigurationDTO> templateConfigurations = templateConfigurationService.fetchByClusterAndConnection(clusterId,connectionId);
		return templateConfigurations;
	}

}
