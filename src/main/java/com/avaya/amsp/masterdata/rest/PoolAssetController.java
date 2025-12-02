package com.avaya.amsp.masterdata.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.ArticlePoolInventoryHistoryDto;
import com.avaya.amsp.masterdata.dtos.PoolAssetDto;
import com.avaya.amsp.masterdata.service.iface.PoolAssetServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/poolAsset")
public class PoolAssetController {
	
	@Autowired
	PoolAssetServiceIface poolAssetService;
	
	/**
	 * This method sets the available quantities of the articles in a pool
	 * 
	 * @param poolId : Pool id
	 * @param articleId : Article id
	 * @param poolAssetDto : details about the article quantities to be updated in a pool
	 * @return
	 */
	@PutMapping("/available")
	@PreAuthorize("hasAnyRole('TK_SV','AVAYA_ADMIN','TK_P')")
	public ResponseEntity<String> setAvlQuantityInPool(@RequestBody PoolAssetDto poolAssetDto ,@AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to set available articles {} in pool {}", poolAssetDto.getArticleId(), poolAssetDto.getPoolId());
		poolAssetDto.setUserName(amspUser.getUsername());
		poolAssetService.setAvailableArticlesInPool(poolAssetDto);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}
	
	/**
	 * This method sets the  reserved quantities of the articles in a pool
	 * 
	 * @param poolId : Pool id
	 * @param articleId : Article id
	 * @param poolAssetDto : details about the article quantities to be updated in a pool
	 * @return
	 */
	@PutMapping("/reserved")
	@PreAuthorize("hasAnyRole('TK_SV','AVAYA_ADMIN','TK_P')")
	public ResponseEntity<String> setResQuantityInPool(@RequestBody PoolAssetDto poolAssetDto) {
		log.info("request received to set reserved articles {} in pool {}", poolAssetDto.getArticleId(), poolAssetDto.getPoolId());
		poolAssetService.setReservedArticlesInPool(poolAssetDto);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}
	
	@GetMapping("/viewChangeHistory/{poolId}")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> viewChangeHistoryForPool(@PathVariable("poolId") String poolId,@AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to view inventory change history for pool id {}", poolId);
		List<ArticlePoolInventoryHistoryDto> changeHistory = poolAssetService.viewChangeHistoryForPool(poolId,amspUser);
		return ResponseEntity.status(HttpStatus.OK).body(changeHistory);
	}
	
}
