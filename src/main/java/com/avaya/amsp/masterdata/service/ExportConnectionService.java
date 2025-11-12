package com.avaya.amsp.masterdata.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

import com.avaya.amsp.domain.ClusterConnection;
import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.Connection;
import com.avaya.amsp.domain.ConnectionI18NName;
import com.avaya.amsp.domain.ConnectionPortType;
import com.avaya.amsp.masterdata.dtos.ExportConnectionDto;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.ClusterConnectionRepository;
import com.avaya.amsp.masterdata.repo.ClusterRepository;

import lombok.extern.slf4j.Slf4j;

@Service("ExportConnectionService")
@Slf4j
public class ExportConnectionService {

	private static final String FILE_PATH = "/tmp/ConnectionData.xlsx";
	private static final String SHEET_NAME = "Connections";
	private static final String DATE_FORMAT = "d/m/yy h:mm";
	
	private static final String EMPTY_STRING = "";

	@Autowired
	private ClusterRepository clusterRepo;
	
	@Autowired
	private ClusterConnectionRepository clstrCntnRepo;

	private static final String[] headerColumns = new String[] {"Cluster key", "Connection key", "Name(German)", 
			"Name(English)", "Remark", "Compatible port types", "Role list", "BCS bunch", "PIN application", 
			"User stamp", "Timestamp(SY)"};

	public byte[] exportConnectionData(Long clusterKey) {
		log.info("Request received to export Connection data for cluster {}", clusterKey);
		List<ExportConnectionDto> connections = getConnectionsForCluster(clusterKey);
		
		XSSFWorkbook workbook = new XSSFWorkbook(); // Create Excel workbook
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME); // Create sheet

		// Create header row 
		createHeaderRow(workbook, sheet); //Populate data
		populateData(sheet, workbook, connections);

		try {
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH));
			workbook.write(out);
			out.close();
			log.info("ConnectionData.xlsx written successfully on disk.");
			workbook.close();

			return getByteArrayFromFile(FILE_PATH);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}

		return null;
	}
	
	public Page<ExportConnectionDto> getConnectionData(Long clusterKey, Pageable pageable) {
		log.info("Request received to get Connection data for cluster {}", clusterKey);
		
		Page<ClusterConnection> clusterConnectionPage = clstrCntnRepo.findByClusterId(clusterKey, pageable);
		
		return clusterConnectionPage.map(this::convertToDto);
	}
	
	private ExportConnectionDto convertToDto(ClusterConnection entity) {
		ExportConnectionDto dto = new ExportConnectionDto();
		Connection connection = entity.getConnection();
		ClusterItem cluster = entity.getCluster();
		
		dto.setClusterKey(cluster.getName());
        dto.setConnectionKey(connection.getName());
        dto.setRemark(connection.getDescription());
        dto.setPinApplication(connection.getPinApplication());
        dto.setBcsBunch(connection.getBcsBunch() != null ? connection.getBcsBunch().getName() : EMPTY_STRING);
        
        for(ConnectionI18NName conni18nName : connection.getConnectionI18NName()) {
        	if(conni18nName.getLanguageId().equals("de")) {
        		dto.setNameNatLang(conni18nName.getTranslation());
        	} else if(conni18nName.getLanguageId().equals("en")) {
        		dto.setNameEnglish(conni18nName.getTranslation());
        	}
		}
        
        String compatiblePortTypes = "";
        for(ConnectionPortType connPortType : connection.getConnectionPortType()) {
			compatiblePortTypes = compatiblePortTypes.concat((connPortType.getPortType() != null && connPortType.getPortType().getName() != null) ? connPortType.getPortType().getName() : "");
			compatiblePortTypes = compatiblePortTypes.concat(", ");
		}
        dto.setCompatiblePortTypes(compatiblePortTypes.length() > 0 ? compatiblePortTypes.substring(0, compatiblePortTypes.length() - 2) : "");
        dto.setRoleList("");
		/*
		 * dto.setLogCreatedBy(connection.getLogCreatedBy());
		 * dto.setLogCreatedOn(connection.getLogCreatedOn());
		 * dto.setLogUpdatedBy(connection.getLogUpdatedBy());
		 * dto.setLogUpdatedOn(connection.getLogUpdatedOn());
		 */
        dto.setUserStamp(connection.getLogUpdatedBy() != null ? connection.getLogUpdatedBy() : connection.getLogCreatedBy());
        dto.setTimeStamp(connection.getLogUpdatedOn() != null ? connection.getLogUpdatedOn() : connection.getLogCreatedOn());
        return dto;
    }
	
	public List<ExportConnectionDto> getConnectionsForCluster(Long clusterKey, Pageable... pageable) {
		log.info("fetching connections for cluster id {}", clusterKey);
		List<ExportConnectionDto> listConnections = new ArrayList<ExportConnectionDto>();
		Optional<ClusterItem> clusterData = clusterRepo.findById(clusterKey);
		
		clusterData.ifPresentOrElse(clusterDataDb -> {
			String clusterName = clusterDataDb.getName();
			Set<ClusterConnection> clusterConnDatadb = clusterDataDb.getClusterConnection();
			Map<String, String> langMap = new HashMap<>();
			clusterConnDatadb.stream().forEach(clusterConnValue -> {
				Connection connValue = clusterConnValue.getConnection();
				for(ConnectionI18NName conni18nName : connValue.getConnectionI18NName()) {
					langMap.put(conni18nName.getLanguageId(), conni18nName.getTranslation());
				}
				
				ExportConnectionDto connectionDto = new ExportConnectionDto();
				if (connValue.getActive() != 0) {
					connectionDto.setClusterKey(clusterName);
					connectionDto.setBcsBunch(connValue.getBcsBunch() != null ? connValue.getBcsBunch().getName() : EMPTY_STRING);
					
					connectionDto.setConnectionKey(connValue.getName());
					
					connectionDto.setNameEnglish(langMap.get("en"));
					connectionDto.setNameNatLang(langMap.get("de"));
					connectionDto.setRemark(connValue.getDescription());
					
					String compatiblePortTypes = "";
					for(ConnectionPortType connPortType : connValue.getConnectionPortType()) {
						compatiblePortTypes = compatiblePortTypes.concat((connPortType.getPortType() != null && connPortType.getPortType().getName() != null) ? connPortType.getPortType().getName() : "");
						compatiblePortTypes = compatiblePortTypes.concat(", ");
					}
					connectionDto.setCompatiblePortTypes(compatiblePortTypes.length() > 0 ? compatiblePortTypes.substring(0, compatiblePortTypes.length() - 2) : "");
					
					connectionDto.setRoleList("");
					connectionDto.setPinApplication(connValue.getPinApplication());
					/*
					 * connectionDto.setLogCreatedBy(connValue.getLogCreatedBy());
					 * connectionDto.setLogCreatedOn(connValue.getLogCreatedOn());
					 * connectionDto.setLogUpdatedBy(connValue.getLogUpdatedBy());
					 * connectionDto.setLogUpdatedOn(connValue.getLogUpdatedOn());
					 */
					connectionDto.setUserStamp(connValue.getLogUpdatedBy() != null ? connValue.getLogUpdatedBy() : connValue.getLogCreatedBy());
					connectionDto.setTimeStamp(connValue.getLogUpdatedOn() != null ? connValue.getLogUpdatedOn() : connValue.getLogCreatedOn());
					listConnections.add(connectionDto);
				} 
			});

		}, () -> {
			log.info("cluster with Id {} not found", clusterKey);
			throw new ResourceNotFoundException(String.format("cluster with Id %s not found", clusterKey));
		});
		return listConnections;
	}

	private void createHeaderRow(XSSFWorkbook workbook, XSSFSheet sheet) {
		Font boldFont = workbook.createFont();
		boldFont.setBold(true);
		CellStyle boldStyle = workbook.createCellStyle();
		boldStyle.setFont(boldFont);

		XSSFRow headerRow = sheet.createRow(0);
		sheet.createFreezePane(0, 1);
		int lastColumnNum = 10;
		sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, lastColumnNum));
		for (int i = 0; i < 11; i++) {
			XSSFCell headerCell = headerRow.createCell(i);
			headerCell.setCellValue(headerColumns[i]);
			headerCell.setCellStyle(boldStyle);
		}
	}

	private void populateData(XSSFSheet sheet, XSSFWorkbook workbook, List<ExportConnectionDto> connections) {
		int rowNum = 1;

		// Create a cell style to accommodate date format 
		CreationHelper helper = workbook.getCreationHelper();
		CellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(helper.createDataFormat().getFormat(DATE_FORMAT));

		for (ExportConnectionDto connection : connections) {

			XSSFRow dataRow = sheet.createRow(rowNum);
			XSSFCell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(connection.getClusterKey());
			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(connection.getConnectionKey());
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(connection.getNameNatLang());
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(connection.getNameEnglish());
			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(connection.getRemark());
			dataCell = dataRow.createCell(5);
			dataCell.setCellValue(connection.getCompatiblePortTypes());
			dataCell = dataRow.createCell(6);
			dataCell.setCellValue(connection.getRoleList());
			dataCell = dataRow.createCell(7);
			dataCell.setCellValue(connection.getBcsBunch());
			dataCell = dataRow.createCell(8);
			dataCell.setCellValue(connection.getPinApplication());
			dataCell = dataRow.createCell(9);
			dataCell.setCellValue(connection.getUserStamp()); 
			dataCell = dataRow.createCell(10);
			dataCell.setCellValue(connection.getTimeStamp());
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
