package com.avaya.amsp.masterdata.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.CompletedOrders;
import com.avaya.amsp.masterdata.dtos.OrderReportDto;
import com.avaya.amsp.masterdata.repo.CompletedOrderItemSpecification;
import com.avaya.amsp.masterdata.repo.CompletedOrderRepository;
import com.avaya.amsp.masterdata.repo.PbxClusterRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("OrderInfoReportService")
public class OrderInfoReportService {

	private static final String SHEET_NAME = "OrderInfo";
	private static final String EMPTY_STRING = "";
	private static final String FILE_PATH_ORDINFO = "/tmp/OrderInfo.xlsx";
	private static final String DATE_FORMAT = "d/m/yy h:mm";

	private static final String[] headerColumns = new String[] {"Order No", "Order Type", "Cluster", "Pbx Cluster",
			"Phone No", "Connection", "Purchaser", "Purchase Date", "Date Completed"};

	@Autowired
	private CompletedOrderRepository orderRepository;
	
	@Autowired
	private PbxClusterRepository pbxClusterRepository;

	/*
	 * Method to create dynamic SQL queries using Spring Data JPA Specification
	 */
	public Page<OrderReportDto> findAllFiltered(Long clusterId, String orderId, String purchaserId, String orderType,
			String connectionId, String phoneNum, Pageable pageable) {

		Specification<CompletedOrders> spec = Specification.where(null);

		if (clusterId != null) {
			spec = spec.and(CompletedOrderItemSpecification.hasClusterId(clusterId));
		}

		if (orderId != null) {
			spec = spec.and(CompletedOrderItemSpecification.hasOrderId(orderId));
		}

		if (purchaserId != null) {
			spec = spec.and(CompletedOrderItemSpecification.hasPurchaserId(purchaserId));
		}

		if (orderType != null) {
			spec = spec.and(CompletedOrderItemSpecification.hasOrderType(orderType));
		}

		if (connectionId != null) {
			spec = spec.and(CompletedOrderItemSpecification.hasConnectionId(connectionId));
		}

		if (phoneNum != null) {
			spec = spec.and(CompletedOrderItemSpecification.hasPhoneNum(phoneNum));
		}

		log.debug("Search Spec: {}", spec);
		Page<CompletedOrders> result = orderRepository.findAll(spec, pageable);
		return result.map(this::convertToDto);
	}

	private OrderReportDto convertToDto(CompletedOrders entity) {
		OrderReportDto orderReportDto = new OrderReportDto();
		orderReportDto.setCluster(entity.getClusterItem() != null ? entity.getClusterItem().getName() : null);
		orderReportDto.setConnection(entity.getConnection() != null ? entity.getConnection().getName() : null);
		orderReportDto.setOrderId(entity.getOrderId());
		orderReportDto.setOrderType(entity.getOrderType().getDescription());
		//orderReportDto.setPbxCluster("" + entity.getPbxClusterId());
		String pbxClusterName = pbxClusterRepository.getClusterName((int) entity.getPbxClusterId().longValue());
	    orderReportDto.setPbxCluster(pbxClusterName != null ? pbxClusterName : "");
	    
		//orderReportDto.setStatus(ENABLED);
		//orderReportDto.setPurchaser(entity.getPurchaser() != null ? entity.getPurchaser().getUsername() : null);
		orderReportDto.setPurchaser(entity.getPurchasedById());
		orderReportDto.setPhone(entity.getPhoneNumber());
		orderReportDto.setOrderDate(entity.getOrderDate());
		orderReportDto.setOrderCompletionDate(entity.getLogTs());
		return orderReportDto;
	}

	public byte[] exportOrderInfo(Long clusterId, String orderId, String purchaserId, String orderType,
			String connectionId, String phoneNum) {
		log.info("Request received to export Order Info");

		Specification<CompletedOrders> spec = Specification.where(null);

		if (clusterId != null) {
			spec = spec.and(CompletedOrderItemSpecification.hasClusterId(clusterId));
		}

		if (orderId != null) {
			spec = spec.and(CompletedOrderItemSpecification.hasOrderId(orderId));
		}

		if (purchaserId != null) {
			spec = spec.and(CompletedOrderItemSpecification.hasPurchaserId(purchaserId));
		}

		if (orderType != null) {
			spec = spec.and(CompletedOrderItemSpecification.hasOrderType(orderType));
		}

		if (connectionId != null) {
			spec = spec.and(CompletedOrderItemSpecification.hasConnectionId(connectionId));
		}

		if (phoneNum != null) {
			spec = spec.and(CompletedOrderItemSpecification.hasPhoneNum(phoneNum));
		}

		log.debug("Search Spec: {}", spec);

		List<CompletedOrders> listOrderInfo = orderRepository.findAll(spec);

		XSSFWorkbook workbook = new XSSFWorkbook(); // Create Excel workbook
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME); // Create sheet

		// Create header row
		createHeaderRow(workbook, sheet);
		// Populate data
		populateData(sheet, workbook, listOrderInfo);

		try {
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH_ORDINFO));
			workbook.write(out);
			out.close();
			log.info("OrderInfo.xlsx written successfully on disk.");
			workbook.close();

			return getByteArrayFromFile(FILE_PATH_ORDINFO);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}

		return null;
	}

	private void createHeaderRow(XSSFWorkbook workbook, XSSFSheet sheet) {
		Font boldFont = workbook.createFont();
		boldFont.setBold(true);
		CellStyle boldStyle = workbook.createCellStyle();
		boldStyle.setFont(boldFont);

		XSSFRow headerRow = sheet.createRow(0);
		sheet.createFreezePane(0, 1);
		int lastColumnNum = 8;
		sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, lastColumnNum));
		for (int i = 0; i <= 8; i++) {
			XSSFCell headerCell = headerRow.createCell(i);
			headerCell.setCellValue(headerColumns[i]);
			headerCell.setCellStyle(boldStyle);
		}
	}

	private void populateData(XSSFSheet sheet, XSSFWorkbook workbook, List<CompletedOrders> listOrderInfo) {
		int rowNum = 1;

		// Create a cell style to accommodate date format
		CreationHelper helper = workbook.getCreationHelper();
		CellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(helper.createDataFormat().getFormat(DATE_FORMAT));

		for (CompletedOrders orderItem : listOrderInfo) {
			XSSFRow dataRow = sheet.createRow(rowNum);

			XSSFCell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(orderItem.getOrderId());
			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(orderItem.getOrderType().getDescription());
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(orderItem.getClusterItem() != null ? orderItem.getClusterItem().getName() : "");
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(orderItem.getPhoneNumber());
			dataCell = dataRow.createCell(5);
			dataCell.setCellValue(orderItem.getConnection() != null ? orderItem.getConnection().getName() : "");
			dataCell = dataRow.createCell(6);
			//dataCell.setCellValue(orderItem.getPurchaser() != null ? orderItem.getPurchaser().getUsername() : "");
			dataCell.setCellValue(orderItem.getPurchasedById());
			dataCell = dataRow.createCell(7);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell.setCellStyle(dateStyle);
			dataCell = dataRow.createCell(8);
			dataCell.setCellValue(orderItem.getLogTs());
			dataCell.setCellStyle(dateStyle);
			
			rowNum++;
		}
	}

	private byte[] getByteArrayFromFile(String filePath) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final InputStream in = new FileInputStream(filePath);
		final byte[] buffer = new byte[500];

		int read = -1;
		while ((read = in.read(buffer)) > 0) {
			baos.write(buffer, 0, read);
		}
		in.close();

		return baos.toByteArray();
	}
	
	public List<String> fetchOrderIdsByCluster(Long clusterid) {
		//LocalDateTime sixMonthsAgo = LocalDateTime.now().minus(6, ChronoUnit.MONTHS);
		return orderRepository.findOrderIdsByClusterId(clusterid);
	}

	public List<String> fetchPurchaserIdsByClusterId(Long clusterid) {
		return orderRepository.findPurchaserIdsByClusterId(clusterid);
	}
}