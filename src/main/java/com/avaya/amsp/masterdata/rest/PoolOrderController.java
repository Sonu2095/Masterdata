package com.avaya.amsp.masterdata.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.PoolOrderRequestDTO;
import com.avaya.amsp.masterdata.dtos.PoolOrderResponseDTO;
import com.avaya.amsp.masterdata.dtos.PoolOrderStatusCode;
import com.avaya.amsp.masterdata.service.iface.PoolOrderServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/poolOrder")
public class PoolOrderController {

	private static final String MEDIA_TYPE_PDF = "application/pdf";

	@Autowired
	PoolOrderServiceIface poolOrderService;

	@PostMapping("/openPoolOrder")
	@PreAuthorize("hasAnyRole('TK_P','AVAYA_ADMIN','TK_SV')")
	public ResponseEntity<Object> openPoolOrder(@RequestBody PoolOrderRequestDTO poolOrderRequestDTO,
			@AuthenticationPrincipal AMSPUser user) {
		log.info("request received to open a new pool order for pool id {}", poolOrderRequestDTO.getPoolId());
		PoolOrderResponseDTO response = poolOrderService.openPoolOrder(poolOrderRequestDTO, user);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PutMapping("/submitPoolOrder")
	@PreAuthorize("hasAnyRole('TK_P','AVAYA_ADMIN','TK_SV')")
	public ResponseEntity<Object> submitPoolOrder(@RequestBody PoolOrderRequestDTO poolOrderRequestDTO,
			@AuthenticationPrincipal AMSPUser user) {
		log.info("request received to submit pool order {} for pool id {}", poolOrderRequestDTO.getOrderId(),
				poolOrderRequestDTO.getPoolId());
		PoolOrderResponseDTO response = poolOrderService.submitPoolOrder(poolOrderRequestDTO, user);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PutMapping("/processSapPoolOrder")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_P','TK_SV')")
	public ResponseEntity<Object> processSapPoolOrder(@RequestBody PoolOrderRequestDTO poolOrderRequestDTO,
			@AuthenticationPrincipal AMSPUser user) {
		log.info("request received to process SAP for pool order {} for pool id {}", poolOrderRequestDTO.getOrderId(),
				poolOrderRequestDTO.getPoolId());
		PoolOrderResponseDTO response = poolOrderService.processSapForPoolOrder(poolOrderRequestDTO, user);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PutMapping("/processShippingPoolOrder")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_P','TK_SV')")
	public ResponseEntity<Object> processShippingPoolOrder(@RequestBody PoolOrderRequestDTO poolOrderRequestDTO,
			@AuthenticationPrincipal AMSPUser user) {
		log.info("request received to process shipping for pool order {} for pool id {}",
				poolOrderRequestDTO.getOrderId(), poolOrderRequestDTO.getPoolId());
		PoolOrderResponseDTO response = poolOrderService.processShippingForPoolOrder(poolOrderRequestDTO, user);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PutMapping("/cancelPoolOrder")
	@PreAuthorize("hasAnyRole('TK_P','AVAYA_ADMIN','TK_SV')")
	public ResponseEntity<Object> cancelPoolOrder(@RequestBody PoolOrderRequestDTO poolOrderRequestDTO,
			@AuthenticationPrincipal AMSPUser user) {
		log.info("request received to cancel pool order {} for pool id {}", poolOrderRequestDTO.getOrderId(),
				poolOrderRequestDTO.getPoolId());
		PoolOrderResponseDTO response = poolOrderService.cancelPoolOrder(poolOrderRequestDTO, user);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PutMapping("/updatePoolOrder")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_P','TK_SV')")
	public ResponseEntity<Object> updatePoolOrder(@RequestBody PoolOrderRequestDTO poolOrderRequestDTO,
			@AuthenticationPrincipal AMSPUser user) {
		log.info("request received to update pool order {} for pool id {}", poolOrderRequestDTO.getOrderId(),
				poolOrderRequestDTO.getPoolId());
		PoolOrderResponseDTO response = poolOrderService.updatePoolOrder(poolOrderRequestDTO, user);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/getPoolOrders")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_P','TK_SV')")
	public ResponseEntity<Object> getPoolOrders(@RequestParam(name = "regionId", required = false) Long regionId,
			@RequestParam(name = "clusterId", required = false) Long clusterId,
			@RequestParam(name = "siteId", required = false) Long siteId,
			@RequestParam(name = "orderItemId", required = false) Long orderItemId,
			@RequestParam(name = "poolId", required = false) Long poolId,
			@RequestParam(name = "poolOrderStatusCode", required = true) PoolOrderStatusCode poolOrderStatusCode) {
		log.info("request received to get pool orders with status {}", poolOrderStatusCode.name());
		if (orderItemId != null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(poolOrderService.getPoolOrderItemsFromId(orderItemId, poolOrderStatusCode));
		} else if (poolId != null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(poolOrderService.getPoolOrderItemsFromPool(poolId, poolOrderStatusCode));
		} else if (siteId != null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(poolOrderService.getPoolOrderItemsFromSite(siteId, poolOrderStatusCode));
		} else if (clusterId != null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(poolOrderService.getPoolOrderItemsFromCluster(clusterId, poolOrderStatusCode));
		} else if (regionId != null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(poolOrderService.getPoolOrderItemsFromRegion(regionId, poolOrderStatusCode));
		} else {
			return ResponseEntity.status(HttpStatus.OK)
					.body(poolOrderService.getPoolOrdersFromStatus(poolOrderStatusCode));
		}
	}

	@GetMapping("/getArticlesPoolOrder")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_P','TK_SV')")
	public ResponseEntity<Object> getArticlesForPoolOrder(
			@RequestParam(name = "poolOrderId", required = true) String poolOrderId,
			@AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to get articles for pool order id {}", poolOrderId);
		return ResponseEntity.status(HttpStatus.OK)
				.body(poolOrderService.getArticlesForPoolOrder(poolOrderId, amspUser));
	}

	@GetMapping("/getFilesPoolOrder")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_P','TK_SV')")
	public ResponseEntity<Object> getFilesForPoolOrder(
			@RequestParam(name = "poolOrderId", required = true) String poolOrderId) {
		log.info("request received to get files for pool order id {}", poolOrderId);
		return ResponseEntity.status(HttpStatus.OK).body(poolOrderService.getFilesForPoolOrder(poolOrderId));
	}

	@GetMapping("/getFile")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_P','TK_SV')")
	public ResponseEntity<?> getFile(@RequestParam(name = "fileId", required = true) String fileId) {
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_PDF))
				.header("Content-Disposition", "attachment; filename=" + fileId + ".pdf")
				.body(poolOrderService.getFile(fileId));
	}
}
