package com.avaya.amsp.masterdata.rest;

import java.util.ArrayList;
import java.util.List;
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

import com.avaya.amsp.masterdata.dtos.ArticleDto;
import com.avaya.amsp.masterdata.dtos.ArticlePropertiesDto;
import com.avaya.amsp.masterdata.dtos.ArticlePropertiesReqDto;
import com.avaya.amsp.masterdata.dtos.ArticlePropertyAttributeDto;
import com.avaya.amsp.masterdata.dtos.ArticleToConnectionReqDto;
import com.avaya.amsp.masterdata.dtos.ClusterDto;
import com.avaya.amsp.masterdata.dtos.ClustersToConnectionDto;
import com.avaya.amsp.masterdata.dtos.ConnectionDto;
import com.avaya.amsp.masterdata.service.ConnectionService;
import com.avaya.amsp.security.user.AMSPUser;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/connections")
public class ConnectionController {

	@Autowired
	ConnectionService connectionService;

	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchConnections() {

		List<ConnectionDto> response = new ArrayList<>();

		log.info("request received to fetch connections from database");
		response = connectionService.fetchAllConnections();
		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	@PostMapping("")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> persistConnection(@Valid @RequestBody ConnectionDto request, @AuthenticationPrincipal AMSPUser amspUser) {

		log.info("request received to persist connection to database {}", request);
		request.setLogCreatedBy(amspUser.getUsername());
		connectionService.persistConnection(request);
		return ResponseEntity.status(HttpStatus.CREATED).body("");

	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> updateConnection(@RequestBody ConnectionDto request,
			@PathVariable("id") Long connectionId, @AuthenticationPrincipal AMSPUser amspUser) {
		request.setLogUpdatedBy(amspUser.getUsername());
		request.setId(connectionId);
		log.info("request received to update connection to database {}", request);
		connectionService.updateConnection(request);
		return ResponseEntity.status(HttpStatus.OK).body("");

	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deleteConnection(@PathVariable("id") Long connectionId) {

		log.info("request received to delete connection from DB with Id as {}", connectionId);
		connectionService.removeConnection(connectionId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}

	/**
	 * This API is use for fetching the master article details for a connection id
	 * 
	 * @param connectionId
	 * @return: 200 OK
	 */

	@GetMapping("/{id}/articles")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchArticles(@PathVariable("id") Long connectionId) {

		List<ArticleDto> response = new ArrayList<>();
		log.info("request received to fetch articles from connection {}", connectionId);
		response = connectionService.fetchArticles(connectionId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * This API is use for assigning the selected article id from ArticleCluster to
	 * a Connection Id
	 * 
	 * @param connectionId
	 * @param articles     : List of selected articleCluster id's
	 * @return 200
	 */
	@PostMapping("/{id}/articles")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> addArticles(@PathVariable("id") Long connectionId,
			@RequestBody @Valid ArticleToConnectionReqDto articles, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to add articles to connection {}. Articles {}", connectionId, articles);
		articles.setUser(amspUser.getUsername());
		connectionService.addArticles(connectionId, articles);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	/**
	 * This API is for updating the article attribute of a Connection id
	 * 
	 * @param connectionId
	 * @param articles
	 * @return
	 */
	@PutMapping("/{id}/articles")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> updateArticles(@PathVariable("id") Long connectionId,
			@RequestBody @Valid ArticleToConnectionReqDto articles, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to update articles to connection {}", connectionId);
		articles.setUser(amspUser.getUsername());
		connectionService.updateArticles(connectionId, articles);
		return ResponseEntity.status(HttpStatus.CREATED).body("");
	}

	/**
	 * API for deleting the assignment of listed article with a connection id
	 * 
	 * @param connectionId
	 * @param articles
	 * @return
	 */
	@DeleteMapping("/{id}/articles")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> removeArticles(@PathVariable("id") Long connectionId,
			@RequestBody @Valid ArticleToConnectionReqDto articles, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to remove articles from connection {}. Articles {}", connectionId, articles);
		articles.setUser(amspUser.getUsername());
		connectionService.removeArticles(connectionId, articles);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}

	/**
	 * API for getting the article properties for a connection
	 * 
	 * @param connectionId
	 * @return
	 */

	@GetMapping("/{id}/articles-properties")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchArticleProperties(@PathVariable("id") Long connectionId) {

		List<ArticlePropertyAttributeDto> response = new ArrayList<>();
		log.info("request received to fetch articles-properties from connection {}", connectionId);
		response = connectionService.fetchArticleProperties(connectionId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * API for getting the article and partlist articles for a connection and
	 * cluster
	 * 
	 * @param connectionId
	 * @param clustId
	 * @return
	 */

	@GetMapping("/{id}/cluster/{clustId}/clusterarticle")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchArticlePropertiesWithInsert(@PathVariable("id") Long connectionId,
			@PathVariable("clustId") Long clusterId) {

		List<ArticlePropertiesDto> response = new ArrayList<>();
		log.info("request received to fetch articles-properties from connection {}", connectionId);
		response = connectionService.fetchArticlePropertiesInsert(connectionId, clusterId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * API for updating the article properties for a connection
	 * 
	 * @param connectionId
	 * @return
	 */
	@PutMapping("/{id}/articles-properties")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Object> updateArticleProperties(@PathVariable("id") Long connectionId,
			@RequestBody ArticlePropertiesReqDto articles, @AuthenticationPrincipal AMSPUser amspUser) {

		List<ArticlePropertiesDto> response = new ArrayList<>();
		log.info("request received to update articles-properties from connection {}", connectionId);
		connectionService.updateArticleProperties(connectionId, articles);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * API for adding the clusters to a connection
	 * 
	 * @param connectionId
	 * @return
	 */
	@PostMapping("/{id}/clusters")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> addClustersToConnection(@PathVariable("id") Long connectionId,
			@RequestBody @Valid ClustersToConnectionDto clusters, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to add clusters to connection {}. cluster {}", connectionId, clusters);
		clusters.setUser(amspUser.getUsername());
		connectionService.addClustersToConnection(connectionId, clusters);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	/**
	 * API for getting the clusters to a connection
	 * 
	 * @param connectionId
	 * @return
	 */

	@GetMapping("/{id}/clusters")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchClustersByConnection(@PathVariable("id") Long connectionId) {

		log.info("request received to fetch all clusters for connection {}", connectionId);
		List<ClusterDto> response = new ArrayList<>();
		response = connectionService.fetchClustersByConnection(connectionId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * API for deleting the clusters to a connection
	 * 
	 * @param connectionId
	 * @return
	 */

	@DeleteMapping("/{id}/clusters")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deleteClustersFromConnection(@PathVariable("id") Long connectionId,
			@RequestBody @Valid ClustersToConnectionDto clusters, @AuthenticationPrincipal AMSPUser amspUser) {
		clusters.setUser(amspUser.getUsername());
		log.info("request received to delete clusters from connection {}", connectionId);
		connectionService.deleteClustersFromConnection(connectionId, clusters);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}
	
	@GetMapping("/{connectionId}/site/{siteId}/articlesPoolView")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getArticlesAsPoolViewForSite(@PathVariable("siteId") Long siteId,
			@PathVariable("connectionId") Long connectionId, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to get articles as pool view for site {}", siteId);
		List<ArticlePropertiesDto> articleData = connectionService.getArticlesAsPoolViewForSite(siteId,
				connectionId, amspUser.getDefaultLanguageId());
		return ResponseEntity.status(HttpStatus.OK).body(articleData);
	}


	@GetMapping("/{id}/cluster/{clusterId}/articles")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getAllArticlesByConnectionId(@PathVariable("id")Long connectionId,@PathVariable("clusterId")Long clusterId,@AuthenticationPrincipal AMSPUser user) {
		log.info("request received to get articles for connectionId {} - clusterId {}", connectionId,clusterId);
		List< ArticlePropertiesDto > articlesByConnectionId = connectionService.getArticlesByConnectionId(connectionId,clusterId,user);
		return ResponseEntity.status(HttpStatus.OK).body(articlesByConnectionId);
	}
}



