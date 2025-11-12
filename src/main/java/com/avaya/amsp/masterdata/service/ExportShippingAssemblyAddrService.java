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
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.Shipping;
import com.avaya.amsp.domain.Site;
import com.avaya.amsp.masterdata.dtos.ShippingAddressDto;
import com.avaya.amsp.masterdata.repo.ShippingAddressRepository;
import com.avaya.amsp.masterdata.repo.SiteRepository;

import lombok.extern.slf4j.Slf4j;

@Service("ExportShippingAssemblyAddrService")
@Slf4j
public class ExportShippingAssemblyAddrService {

	private static final String FILE_PATH = "/tmp/ShippingAssemblyAddressData.xlsx";
	private static final String SHEET_NAME = "ShippingAssemblyAddress";
	private static final String DATE_FORMAT = "d/m/yy h:mm";

	@Autowired
	private ShippingAddressRepository shippingAddressRepo;
	
	@Autowired
	private SiteRepository siteRepo;

	private static final String[] headerColumns = new String[] {"Cluster key", "Site key", "Company name", 
			"Consignee", "Street", "Postcode", "City", "Country", "Building", 
			"Room", "Contact partner", "Phone", "Shipping type", "Remark", "User stamp", "Timestamp(SY)"};

	public byte[] exportShippingData(Long siteKey) {
		log.info("Request received to export Shipping Assembly Address data for site {}", siteKey);
		
		Site site = siteRepo.findById(siteKey).get();
		List<Shipping> shippingAddressList = shippingAddressRepo.findBySiteId(siteKey);
		
		XSSFWorkbook workbook = new XSSFWorkbook(); // Create Excel workbook
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME); // Create sheet

		// Create header row 
		createHeaderRow(workbook, sheet); //Populate data
		populateData(sheet, workbook, shippingAddressList, site.getClusterItem().getName());

		try {
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH));
			workbook.write(out);
			out.close();
			log.info("ShippingAssemblyAddressData.xlsx written successfully on disk.");
			workbook.close();

			return getByteArrayFromFile(FILE_PATH);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}

		return null;
	}
	
	public Page<ShippingAddressDto> getShippingData(Long siteKey, Pageable pageable) {
		log.info("Request received to get Shipping Assembly Address data for site {}", siteKey);
		Page<Shipping> shippingAddressList = shippingAddressRepo.findBySiteId(siteKey, pageable);

		return shippingAddressList.map(this::convertToDto);
	}
	
	private ShippingAddressDto convertToDto(Shipping entity) {
		ShippingAddressDto dto = new ShippingAddressDto();
		Site site = entity.getSite();
        
		dto.setSiteName(site.getName());
		dto.setClusterName(site.getClusterItem().getName());
		dto.setCompanyName(entity.getCompanyName());
		dto.setConsignee(entity.getConsignee());
		dto.setStreet(entity.getStreet());
		dto.setPostcode(entity.getPostcode());
		dto.setCity(entity.getCity());
		dto.setCountryName(entity.getCountry().getName());
		dto.setBuilding(entity.getBuilding());
		dto.setRoom(entity.getRoom());
		dto.setContactPartner(entity.getContactPartner());
		dto.setPhone(entity.getPhone());
		dto.setDescription(entity.getDescription());
		dto.setShippingType(entity.getShippingType());
		dto.setUserStamp(entity.getLogUpdatedBy() != null ? entity.getLogUpdatedBy() : entity.getLogCreatedBy());
        dto.setTimeStamp(entity.getLogUpdatedOn() != null ? entity.getLogUpdatedOn() : entity.getLogCreatedOn());
		
        return dto;
    }

	private void createHeaderRow(XSSFWorkbook workbook, XSSFSheet sheet) {
		Font boldFont = workbook.createFont();
		boldFont.setBold(true);
		CellStyle boldStyle = workbook.createCellStyle();
		boldStyle.setFont(boldFont);

		XSSFRow headerRow = sheet.createRow(0);
		sheet.createFreezePane(0, 1);
		int lastColumnNum = 15;
		sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, lastColumnNum));
		for (int i = 0; i < 16; i++) {
			XSSFCell headerCell = headerRow.createCell(i);
			headerCell.setCellValue(headerColumns[i]);
			headerCell.setCellStyle(boldStyle);
		}
	}

	private void populateData(XSSFSheet sheet, XSSFWorkbook workbook, List<Shipping> shippingAddressList, String cluster) {
		int rowNum = 1;

		// Create a cell style to accommodate date format 
		CreationHelper helper = workbook.getCreationHelper();
		CellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(helper.createDataFormat().getFormat(DATE_FORMAT));

		for (Shipping shipping : shippingAddressList) {

			XSSFRow dataRow = sheet.createRow(rowNum);
			XSSFCell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(cluster);
			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(shipping.getSite().getName());
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(shipping.getCompanyName());
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(shipping.getConsignee());
			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(shipping.getStreet());
			dataCell = dataRow.createCell(5);
			dataCell.setCellValue(shipping.getPostcode());
			dataCell = dataRow.createCell(6);
			dataCell.setCellValue(shipping.getCity());
			dataCell = dataRow.createCell(7);
			dataCell.setCellValue(shipping.getCountry().getName());
			dataCell = dataRow.createCell(8);
			dataCell.setCellValue(shipping.getBuilding());
			dataCell = dataRow.createCell(9);
			dataCell.setCellValue(shipping.getRoom());
			dataCell = dataRow.createCell(10);
			dataCell.setCellValue(shipping.getContactPartner());
			dataCell = dataRow.createCell(11);
			dataCell.setCellValue(shipping.getPhone());
			dataCell = dataRow.createCell(12);
			dataCell.setCellValue(shipping.getShippingType() != null ? shipping.getShippingType().getName() : "");
			dataCell = dataRow.createCell(13);
			dataCell.setCellValue(shipping.getDescription());
			dataCell = dataRow.createCell(14);
			dataCell.setCellValue(shipping.getLogUpdatedBy() != null ? shipping.getLogUpdatedBy() : shipping.getLogCreatedBy()); 
			dataCell = dataRow.createCell(15);
			dataCell.setCellValue(shipping.getLogUpdatedOn() != null ? shipping.getLogUpdatedOn() : shipping.getLogCreatedOn());
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

}
