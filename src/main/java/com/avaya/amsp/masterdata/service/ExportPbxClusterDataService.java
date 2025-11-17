package com.avaya.amsp.masterdata.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
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

import com.avaya.amsp.domain.PbxCluster;
import com.avaya.amsp.masterdata.dtos.PbxClusterDto;
import com.avaya.amsp.masterdata.repo.PbxClusterRepository;

import lombok.extern.slf4j.Slf4j;

@Service("ExportPbxClusterDataService")
@Slf4j
public class ExportPbxClusterDataService {
	
	private static final String FILE_PATH = "/tmp/PbxClusterData.xlsx";
	private static final String SHEET_NAME = "PbxClusters";
	private static final String DATE_FORMAT = "d/m/yy h:mm";
	private static final String EMPTY_STRING = "";
	
	@Autowired
	private PbxClusterRepository pbxClusterRepository;
	
	private static final String[] headerColumns = new String[]{"Cluster key", "PBX cluster key", "Description", "Area code", "Area code info",
			"Country", "PBX id", "User stamp", "Timestamp(SY)"};
	
	public byte[] exportPbxClusterData(Long clusterId) {
		log.info("Request received to export PbxClusterData");
		List<PbxCluster> pbxClusterItems = pbxClusterRepository.findByClusterId(clusterId);
		
		XSSFWorkbook workbook = new XSSFWorkbook(); // Create Excel workbook
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME); //Create sheet
		
		//Create header row
		createHeaderRow(workbook, sheet);
		//Populate data
		populateData(sheet, workbook, pbxClusterItems);
		
		try {
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH));
			workbook.write(out);
			out.close();
			log.info("PbxClusterData.xlsx written successfully on disk.");
			workbook.close();
			
			return getByteArrayFromFile(FILE_PATH);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}
		
		return null;
	}
	
	public Page<PbxClusterDto> getPbxClusterData(Long clusterId, Pageable pageable) {
		log.info("Request received to get PbxClusterData");
		Page<PbxCluster> pbxClusterData = pbxClusterRepository.findByClusterId(clusterId, pageable);
		return pbxClusterData.map(this::convertToDto);
	}
	
	private PbxClusterDto convertToDto(PbxCluster entity) {
		PbxClusterDto dto = new PbxClusterDto();
    	dto.setAreacode(entity.getAreacode());
    	dto.setAreacodeInfo(entity.getAreacodeInfo());
    	dto.setClusterKey(entity.getClusterItem() != null ? entity.getClusterItem().getName() : EMPTY_STRING);
    	dto.setCountryName(entity.getCountry() != null ? entity.getCountry().getName() : EMPTY_STRING);
    	//dto.setCountryCode(entity.getCountry() != null ? entity.getCountry().getCountry_Code() : EMPTY_STRING);
    	dto.setDescriptionEnglish(entity.getDescriptionEnglish());
		/*
		 * dto.setLogCreatedBy(entity.getLogCreatedBy());
		 * dto.setLogCreatedOn(entity.getLogCreatedOn());
		 * dto.setLogUpdatedBy(entity.getLogUpdatedBy());
		 * dto.setLogUpdatedOn(entity.getLogUpdatedOn());
		 */
    	dto.setUserStamp(entity.getLogUpdatedBy() != null ? entity.getLogUpdatedBy() : entity.getLogCreatedBy());
        dto.setTimeStamp(entity.getLogUpdatedOn() != null ? Timestamp.valueOf(entity.getLogUpdatedOn()) : Timestamp.valueOf(entity.getLogCreatedOn()));
    	dto.setName(entity.getName());
    	dto.setPbxId(entity.getPbxId());
    	return dto;
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
		for(int i = 0; i < 9; i++) {
			XSSFCell headerCell = headerRow.createCell(i);
			headerCell.setCellValue(headerColumns[i]);
			headerCell.setCellStyle(boldStyle);
		}
	}
	
	private void populateData(XSSFSheet sheet, XSSFWorkbook workbook, List<PbxCluster> pbxClusterItems) {
		int rowNum = 1;
		
		//Create a cell style to accommodate date format
		CreationHelper helper = workbook.getCreationHelper();
		CellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(helper.createDataFormat().getFormat(DATE_FORMAT));
		
		for(PbxCluster pbxClusterItem : pbxClusterItems) {
			XSSFRow dataRow = sheet.createRow(rowNum);
			XSSFCell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(pbxClusterItem.getClusterItem() != null ? pbxClusterItem.getClusterItem().getName() : EMPTY_STRING);
			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(pbxClusterItem.getName());
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(pbxClusterItem.getDescriptionEnglish());
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(pbxClusterItem.getAreacode());
			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(pbxClusterItem.getAreacodeInfo());
			dataCell = dataRow.createCell(5);
			dataCell.setCellValue(pbxClusterItem.getCountry() != null ? pbxClusterItem.getCountry().getName() : EMPTY_STRING);
			dataCell = dataRow.createCell(6);
			dataCell.setCellValue(pbxClusterItem.getPbxId());
			dataCell = dataRow.createCell(7);
			dataCell.setCellValue(pbxClusterItem.getLogUpdatedBy() != null ? pbxClusterItem.getLogUpdatedBy() : pbxClusterItem.getLogCreatedBy());
			dataCell = dataRow.createCell(8);
			dataCell.setCellValue(pbxClusterItem.getLogUpdatedOn() != null ? pbxClusterItem.getLogUpdatedOn() : pbxClusterItem.getLogCreatedOn());
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
