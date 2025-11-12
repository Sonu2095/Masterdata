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

import com.avaya.amsp.masterdata.dtos.ArticleToClusterDto;
import com.avaya.amsp.masterdata.dtos.PoolDto;
import com.avaya.amsp.masterdata.dtos.PoolToArticleDto;
import com.avaya.amsp.masterdata.service.PoolService;
import com.avaya.amsp.security.user.AMSPUser;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/pools")
public class PoolController {

	@Autowired
	PoolService poolService;

	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchPools() {

		log.info("requesting for getting all available pools");
		List<PoolDto> response = new ArrayList<>();
		response = poolService.fetchAllPools();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PostMapping("")
	@PreAuthorize("hasAnyRole('TK_SV','AVAYA_ADMIN')")
	public ResponseEntity<String> persistPools(@Valid @RequestBody PoolDto request, @AuthenticationPrincipal AMSPUser amspUser) {
		request.setLogCreatedBy(amspUser.getUsername());
		log.info("request received to create a new pool{}", request);
		poolService.createPool(request);
		return ResponseEntity.status(HttpStatus.CREATED).body("");
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('TK_SV','AVAYA_ADMIN')")
	public ResponseEntity<String> updatePools(@RequestBody PoolDto request, @PathVariable("id") Long poolId, @AuthenticationPrincipal AMSPUser amspUser) {

		request.setId(poolId);
		request.setLogUpdatedBy(amspUser.getUsername());
		log.info("request received to update pool {}", request);
		poolService.updatePool(request);
		return ResponseEntity.status(HttpStatus.OK).body("");

	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('TK_SV','AVAYA_ADMIN')")
	public ResponseEntity<String> deletePools(@PathVariable("id") Long poolId) {

		log.info("request received to delete pool {}", poolId);
		poolService.deletePool(poolId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}

	/**
	 * This method add the selected articles to the selected pool
	 * 
	 * @param siteId -- site id from request
	 * @param pools  - List of pool ids
	 * @return
	 */
	@PostMapping("/{id}/articles")
	@PreAuthorize("hasAnyRole('TK_SV','AVAYA_ADMIN')")
	public ResponseEntity<String> addArticlesToPool(@PathVariable("id") Long poolId,
			@RequestBody @Valid PoolToArticleDto articles, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to add articles to pool {} for pool Id {}", poolId);
		poolService.addAssignArticles(poolId, articles, amspUser);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	/**
	 * This method will delete the articles from assigned pools
	 * 
	 * @param siteId : pool id
	 * @param pools  : list of all articles
	 * @return
	 */
	//TODO: Rename the method
	@DeleteMapping("/{id}/articles")
	@PreAuthorize("hasAnyRole('TK_SV','AVAYA_ADMIN')")
	public ResponseEntity<String> deleteArticlesFromPool(@PathVariable("id") Long poolId,
			@RequestBody @Valid PoolToArticleDto articles, @AuthenticationPrincipal AMSPUser amspUser) {

		log.info("request received to delete articles to pool {} for pool Id {}", poolId);
		poolService.deleteAssignArticles(poolId, articles, amspUser);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	/**
	 * This method fetches the articles for a specific pool Id
	 * 
	 * @param siteId : Pool id
	 * @param pools  : list of all associated articles
	 * @return
	 */
	@GetMapping("/{poolId}/articles")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getArticlesForPool(@PathVariable("poolId") Long poolId) {
		log.info("request received to get aticles for pool {}", poolId);
		List<ArticleToClusterDto> articledata = poolService.getArticlesForPool(poolId);
		return ResponseEntity.status(HttpStatus.OK).body(articledata);
	}

}
