package com.avaya.amsp.masterdata.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.OrderReportDto;
import com.avaya.amsp.masterdata.service.OrderInfoReportService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("v1")
@RestController
public class ReportController {
	
	private static final String MEDIA_TYPE_EXCEL = "application/vnd.ms-excel";
	
	private static final String ORDER_INFO_FILE_NAME = "OrderInfo.xlsx";
	
	@Autowired
	private OrderInfoReportService orderInfoReportService;
	
	@GetMapping("/report/view/orderInfo")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getOrderInfo(@RequestParam int page, 
	        @RequestParam int size, 
	        @RequestParam(required = false) String[] sort,
	        @RequestParam(name = "clusterId", required = true) Long clusterId,
	        @RequestParam(name = "orderId", required = false) String orderId,
	        @RequestParam(name = "purchaser", required = false) String purchaserId,
	        @RequestParam(name = "orderType", required = false) String orderType,
	        @RequestParam(name = "connectionId", required = false) String connectionId,
	        @RequestParam(name = "phoneNum", required = false) String phoneNum) {
		
		log.info("Request received to get order items for reporting");
		Sort sortObject = Sort.by(sort != null ? sort : new String[0]);
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<OrderReportDto> orderItems = orderInfoReportService.findAllFiltered(clusterId, orderId, 
        		purchaserId, orderType, connectionId, phoneNum, pageable);
        if (orderItems == null || orderItems.isEmpty()) {
        	orderItems = Page.empty(pageable); // create an empty page with pagination info
        }
        
		return ResponseEntity.ok(orderItems);
	}
	
	@GetMapping("/report/export/orderInfo")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> exportOrderInfo(@RequestParam(name = "clusterId", required = true) Long clusterId,
			@RequestParam(name = "orderId", required = false) String orderId,
			@RequestParam(name = "purchaser", required = false) String purchaserId,
			@RequestParam(name = "orderType", required = false) String orderType,
			@RequestParam(name = "connectionId", required = false) String connectionId,
			@RequestParam(name = "phoneNum", required = false) String phoneNum) {
		log.info("Request received to export order items for reporting");
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL))
				.header("Content-Disposition", "attachment; filename=" + ORDER_INFO_FILE_NAME)
				.body(orderInfoReportService.exportOrderInfo(clusterId, orderId, purchaserId, orderType, connectionId,
						phoneNum));
	}
	
	@GetMapping("/report/{clusterId}/orderIds")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getOrderIds(@PathVariable("clusterId") Long clusterId) {
		log.info("Request received to get order ids for cluster {}", clusterId);
		List<String> orderIds = orderInfoReportService.fetchOrderIdsByCluster(clusterId);
		return ResponseEntity.ok(orderIds);
	}
	
	@GetMapping("/report/{clusterId}/purchaserIds")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getPurchaserIds(@PathVariable("clusterId") Long clusterId) {
		log.info("Request received to get purchaser ids for cluster {}", clusterId);
		List<String> listPurchaser = orderInfoReportService.fetchPurchaserIdsByClusterId(clusterId);
		return ResponseEntity.ok(listPurchaser);
	}
}
