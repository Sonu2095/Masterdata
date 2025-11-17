package com.avaya.amsp.masterdata.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

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

import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.PbxSystem;
import com.avaya.amsp.masterdata.dtos.PbxSystemDto;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.ClusterRepository;
import com.avaya.amsp.masterdata.repo.PbxSystemRepository;

import lombok.extern.slf4j.Slf4j;

@Service("ExportPbxSystemDataService")
@Slf4j
public class ExportPbxSystemDataService {
	
	private static final String FILE_PATH = "/tmp/PbxSystemData.xlsx";
	private static final String SHEET_NAME = "PbxSystems";
	private static final String DATE_FORMAT = "d/m/yy h:mm";
	
	@Autowired
	private PbxSystemRepository pbxSystemRepository;
	
	@Autowired
	private ClusterRepository clusterRepo;
	
	private static final String[] headerColumns = new String[]{"Cluster key","Physical PBX", "AEM PBX","Sfb system","Teams system","FL code","Shipping email id",
			"Assembling email id","SIP Domain","Remark","Routing policy destination","CM name","Notes","ARS Analysis Entry","User stamp",
			"Timestamp(SY)"};
	
	public byte[] exportPbxSystemData(Long clusterId) {
		log.info("Request received to export PbxSystemData");
		
		Optional<ClusterItem> clusterData = clusterRepo.findById(clusterId);
		if(clusterData == null) {
			log.info("cluster with Id {} not found", clusterId);
			throw new ResourceNotFoundException(String.format("cluster with Id %s not found", clusterId));
		}
		List<PbxSystem> listPbxSystem = pbxSystemRepository.findByCluster_Id(clusterId);

		XSSFWorkbook workbook = new XSSFWorkbook(); // Create Excel workbook
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME); // Create sheet

		// Create header row 
		createHeaderRow(workbook, sheet); 
		//Populate data
		populateData(sheet, workbook, listPbxSystem, clusterData.get().getName());

		try {
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH));
			workbook.write(out);
			out.close();
			log.info("PbxSystemData.xlsx written successfully on disk.");
			workbook.close();

			return getByteArrayFromFile(FILE_PATH);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}
		
		return null;
	}
	
	public Page<PbxSystemDto> getPbxSystemData(Long clusterId, Pageable pageable) {
		log.info("Request received to get PbxSystemData");
		Page<PbxSystem> pbxSystems = pbxSystemRepository.findByCluster_Id(clusterId, pageable);
		return pbxSystems.map(this::convertToDto);
	}
	
	private PbxSystemDto convertToDto(PbxSystem entity) {
		PbxSystemDto dto = new PbxSystemDto();
    	dto.setAemPbx(entity.getAemPbx());
    	dto.setArsAnalysisEntry(entity.isArsAnalysisEntry());
    	dto.setAssemblingEmailId(entity.getAssemblingEmailId());
    	dto.setCmName(entity.getCmName());
    	dto.setFlCode(entity.getFlCode());
    	dto.setNotes(entity.getNotes());
    	dto.setTeamsSystem(entity.isTeamsSystem());
    	dto.setSfbsSystem(entity.isSfbsSystem());
    	dto.setSipDomain(entity.getSipDomain());
    	dto.setRoutingPolicyName(entity.getRoutingPolicyName());
    	dto.setRemark(entity.getRemark());
    	dto.setPhysicalPbx(entity.getPhysicalPbx());
    	dto.setShippingEmailId(entity.getShippingEmailId());
		/*
		 * dto.setLogUpdatedOn(entity.getLogUpdatedOn());
		 * dto.setLogUpdatedBy(entity.getLogUpdatedBy());
		 * dto.setLogCreatedOn(entity.getLogCreatedOn());
		 * dto.setLogCreatedBy(entity.getLogCreatedBy());
		 */
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
		for(int i = 0; i < 16; i++) {
			XSSFCell headerCell = headerRow.createCell(i);
			headerCell.setCellValue(headerColumns[i]);
			headerCell.setCellStyle(boldStyle);
		}
	}
	
	private void populateData(XSSFSheet sheet, XSSFWorkbook workbook, List<PbxSystem> listPbxSystem, String clusterKey) {
		int rowNum = 1;
		
		//Create a cell style to accommodate date format
		CreationHelper helper = workbook.getCreationHelper();
		CellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(helper.createDataFormat().getFormat(DATE_FORMAT));
		
		for(PbxSystem pbxSystem : listPbxSystem) {
			XSSFRow dataRow = sheet.createRow(rowNum);
			XSSFCell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(clusterKey);
			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(pbxSystem.getPhysicalPbx());
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(pbxSystem.getAemPbx());
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(pbxSystem.isSfbsSystem());
			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(pbxSystem.isTeamsSystem());
			dataCell = dataRow.createCell(5);
			dataCell.setCellValue(pbxSystem.getFlCode());
			dataCell = dataRow.createCell(6);
			dataCell.setCellValue(pbxSystem.getShippingEmailId());
			dataCell = dataRow.createCell(7);
			dataCell.setCellValue(pbxSystem.getAssemblingEmailId());
			dataCell = dataRow.createCell(8);
			dataCell.setCellValue(pbxSystem.getSipDomain());
			dataCell = dataRow.createCell(9);
			dataCell.setCellValue(pbxSystem.getRemark()); //TODO: Populate empty fields
			dataCell = dataRow.createCell(10);
			dataCell.setCellValue(pbxSystem.getRoutingPolicyName());
			dataCell = dataRow.createCell(11);
			dataCell.setCellValue(pbxSystem.getCmName());
			dataCell = dataRow.createCell(12);
			dataCell.setCellValue(pbxSystem.getNotes());
			dataCell = dataRow.createCell(13);
			dataCell.setCellValue(pbxSystem.isArsAnalysisEntry());
			dataCell = dataRow.createCell(14);
			dataCell.setCellValue(pbxSystem.getLogUpdatedBy() != null ? pbxSystem.getLogUpdatedBy() : pbxSystem.getLogCreatedBy());
			dataCell = dataRow.createCell(15);
			dataCell.setCellValue(pbxSystem.getLogUpdatedOn() != null ? pbxSystem.getLogUpdatedOn() : pbxSystem.getLogCreatedOn());
			
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
