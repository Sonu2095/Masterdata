package com.avaya.amsp.masterdata.rest;

import java.util.ArrayList;
import java.util.List;

import com.avaya.amsp.masterdata.dtos.*;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.service.iface.ArticleServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/articles")
public class ArticleController {

	@Autowired
	private ArticleServiceIface articleService;

	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchArticles() {

		List<ArticleDto> response = new ArrayList<>();
		log.info("request received to fetch artilces from database");
		response = articleService.fetchAllArticles();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/{partStatus}")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchArticlesByParts(@PathVariable("partStatus") int partStatus) {

		List<ArticlePartListDto> response = new ArrayList<>();
		log.info("request received to fetch artilces from database for parts enabled/disabled");
		response = articleService.fetchAllArticlesByParts(partStatus);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/{leadArticleId}/subarticles")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchSubArticles(@PathVariable("leadArticleId") Long leadArticleId) {
		List<ArticleDto> response = new ArrayList<>();
		log.info("request received to fetch sub articles for a lead article");
		response = articleService.fetchSubArticles(leadArticleId);
		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	/**
	 * This method will be used to get the clusters based on the specific article id
	 * 
	 * @param articleId : article Id passed from UI
	 * @return
	 */
	@GetMapping("/{id}/mastermerge/clusters")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchMasterMergeCluster(@PathVariable("id") Long articleId) {
		log.info("Request received to fetch all clusters for article {}", articleId);
		List<ClusterDto> clusters = articleService.fetchMasterMergeClusters(articleId);
		return ResponseEntity.status(HttpStatus.OK).body(clusters);
	}

	/**
	 * This method will be used to get the clusters based on the specific article id
	 * 
	 * @param articleId : article Id passed from UI
	 * @return
	 */
	@GetMapping("/{id}/slavemerge/clusters")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchSlaveMergeCluster(@PathVariable("id") Long articleId) {
		log.info("Request received to fetch all clusters for article {}", articleId);
		List<ClusterDto> clusters = articleService.fetchSlaveMergeClusters(articleId);
		return ResponseEntity.status(HttpStatus.OK).body(clusters);
	}

	@PostMapping("")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public String persistArticle(@Valid @RequestBody ArticleDto request, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to persist artilce to database {}", request);
		request.setUser(amspUser.getUsername());
		String status = articleService.persistArticle(request);
		return status;

	}

	/**
	 * This method will be used for adding the clusters to an article
	 * 
	 * @param articleId : articleId
	 * @param clusters  : List of cluster ids needs to be added
	 * @return
	 */

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> updateArticle(@RequestBody ArticleDto request, @PathVariable("id") Long articleId, @AuthenticationPrincipal AMSPUser amspUser) {

		log.info("request received to update article to database {}", request);
		request.setId(articleId);
		request.setUser(amspUser.getUsername());
		articleService.updateArticle(request);
		return ResponseEntity.status(HttpStatus.OK).body("");

	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deleteArticle(@PathVariable("id") Long articleId) {

		log.info("request received to delete article from DB with Id as {}", articleId);
		articleService.removeArticle(articleId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}

	// Added for Adding clusters to Articles

	/**
	 * This is for adding/assigning articles to cluster
	 * 
	 * @param articleId
	 * @param clusters
	 * @return
	 */

	@PostMapping("/{id}/clusters")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> addClustersToArticle(@PathVariable("id") Long articleId,
			@RequestBody @Valid ClusterArticleDetailDto clusters, @AuthenticationPrincipal AMSPUser amspUser ) {
		log.info("request received to add clusters {} to articleId {} ", clusters, articleId);
		clusters.setUser(amspUser.getUsername());
		articleService.addClustersToArticle(articleId, clusters);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	/**
	 * This method is for adding the master linkage
	 * 
	 * @param articleId
	 * @param clusters
	 * @return
	 */

	@PutMapping("/{id}/clusters/removemasterlinkage")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> updateMasterToSlave(@PathVariable("id") Long articleId,
			@RequestBody @Valid ClusterArticleDetailDto clusters, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to add master linkage for clusters {} to articleId {} ", clusters, articleId);
		clusters.setUser(amspUser.getUsername());
		articleService.updateMasterToSlave(articleId, clusters);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	/**
	 * This method is for removing the master linkage
	 * 
	 * @param articleId
	 * @param clusters
	 * @return
	 */

	@PutMapping("/{id}/clusters/addmasterlinkage")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> updateSlaverToMaster(@PathVariable("id") Long articleId,
			@RequestBody @Valid ClusterArticleDetailDto clusters, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to remove master linkage for clusters {} to articleId {} ", clusters, articleId);
		clusters.setUser(amspUser.getUsername());
		articleService.updateSlaveToMaster(articleId, clusters);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PostMapping("/{id}/subarticles")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> addSubArticlesToLeadArticle(@PathVariable("id") Long articleId,
			@RequestBody @Valid SubArticleDetailDto subArticles, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to add sub articles {} to lead articleId {} ", subArticles, articleId);
		articleService.addSubArticlesToArticle(articleId, subArticles, amspUser.getUsername());
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@DeleteMapping("/{id}/subarticles")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deleteSubArticles(@PathVariable("id") Long articleId,
			@RequestBody @Valid SubArticleDetailDto subArticlesDto, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to delete subarticle for a lead article {}", articleId);
		articleService.deleteAssignSubArticles(articleId, subArticlesDto, amspUser.getUsername());
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}

	@DeleteMapping("/leadarticle{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deleteLeadArticle(@PathVariable("id") Long leadId) {
		log.info("request received to delete a lead article {}", leadId);
		articleService.deleteLeadArticles(leadId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}

	// cluster partlist
// for getting all the clusters for a kead articles having is_master_partlist = 0/1
	@GetMapping("/{id}/partlist/{masterStatus}/clusters")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchClusterDetailByMasterStatus(@PathVariable("id") Long articleId,
			@PathVariable("masterStatus") int masterStatus) {
		log.info(
				"Request received to fetch all clusters for article-clusters {} with as per master status/slave status {}",
				articleId, masterStatus);
		List<ClusterDto> connections = articleService.fetchClusterDetailsByPartMasterStatus(articleId, masterStatus);
		return ResponseEntity.status(HttpStatus.OK).body(connections);
	}

	@GetMapping("/{id}/{partStatus}/clusters")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchClusterDetailByParts(@PathVariable("id") Long articleId,
			@PathVariable("partStatus") int partStatus) {
		log.info("Request received to fetch all clusters for article-clusters {}with part status {}", articleId,
				partStatus);
		List<ClusterDto> connections = articleService.fetchClusterDetailsByParts(articleId, partStatus);
		return ResponseEntity.status(HttpStatus.OK).body(connections);
	}

	/*
	 * This endpoint is used for updating clusters for an Article to Master Part
	 * list use in Master->partlist->extras
	 */

	@PutMapping("/{id}/clusters/partlist/assignmasterstatus")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> assignMasterStatus(@PathVariable("id") Long articleId,
			@RequestBody @Valid ClusterArticleDetailDto clusters, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to update master partlist status in cluster partlist for cluster and article {} ",
				clusters, articleId);
		clusters.setUser(amspUser.getUsername());
		articleService.addPartListMasterStatus(articleId, clusters);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PutMapping("/{id}/clusters/partlist/removemasterstatus")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> removeMasterStatus(@PathVariable("id") Long articleId,
			@RequestBody @Valid ClusterArticleDetailDto clusters, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to remove master partlist status in cluster partlist for cluster and article {} ",
				clusters, articleId);
		clusters.setUser(amspUser.getUsername());
		articleService.removePartListMasterStatus(articleId, clusters);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	/*
	 * Added to set the master status, insert data in articlecluster subarticle and
	 * insert data to article cluster subarticle table
	 */
	@PostMapping("/{id}/partlist/clusters")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> addClustersToLeadArticle(@PathVariable("id") Long articleId,
			@RequestBody @Valid ClusterArticleDetailDto clusters, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to add clusters {} to lead article Id {} ", clusters, articleId);
		clusters.setUser(amspUser.getUsername());
		articleService.addPartListArticlesToClusters(articleId,clusters);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}
	
	//cluster partlist for synching -- adding clusters to lead article for synching process..
	
	@PostMapping("/{id}/partlist/clusters/synch")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> addClustersToLeadArticleSync(@PathVariable("id") Long articleId,
			@RequestBody @Valid ClusterArticleDetailDto clusters, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("sync request received to add clusters {} to lead articleId {} ", clusters, articleId);
		clusters.setUser(amspUser.getUsername());
		articleService.addPartListArticlesToClusters(articleId, clusters);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}


	@PostMapping("/{id}/sync/article-clusters")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Object> syncMasterArticleToClusters(@PathVariable("id") Long articleId , @RequestBody @Valid ArticleToClusterSyncDto payload, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to sync master article with article cluster for article {} with payload as {}",articleId,payload);
		articleService.syncMasterArticlesToClusters(articleId,payload, amspUser.getUsername());
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@GetMapping("/{id}/articleI18Name")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getArticleI18Names(@PathVariable("id") Long articleId) {
		log.info("Request received to get Article names for article id: {}", articleId);
		ArticleI18NameDTO response = articleService.fetchArticleLabel(articleId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	/**
	 * This method will be used to get the unassignedClusters based on the specific
	 * article id in AddMaster Tab
	 * 
	 * @param articleId : article Id passed from UI
	 * @return
	 */
	@GetMapping("/{id}/unassignedClusters")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getUnassignedClusters(@PathVariable("id") Long articleId) {
		log.info("Request received to fetch all unassigned clusters for article {}", articleId);
		List<ClusterDto> clusters = articleService.fetchUnassignedClusters(articleId);
		return ResponseEntity.status(HttpStatus.OK).body(clusters);
	}
}
