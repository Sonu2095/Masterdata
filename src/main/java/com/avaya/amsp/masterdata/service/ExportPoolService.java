package com.avaya.amsp.masterdata.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

import com.avaya.amsp.domain.Pool;
import com.avaya.amsp.domain.Site;
import com.avaya.amsp.domain.SitePool;
import com.avaya.amsp.masterdata.dtos.ExportPoolDto;
import com.avaya.amsp.masterdata.repo.SitePoolRepository;
import com.avaya.amsp.masterdata.repo.SiteRepository;

import lombok.extern.slf4j.Slf4j;

@Service("ExportPoolService")
@Slf4j
public class ExportPoolService {

	private static final String FILE_PATH = "/tmp/PoolData.xlsx";
	private static final String SHEET_NAME = "Pools";
	private static final String DATE_FORMAT = "d/m/yy h:mm";

	@Autowired
	private SiteRepository siteRepo;
	
	@Autowired
	private SitePoolRepository sitePoolRepo;

	private static final String[] headerColumns = new String[] {"Cluster key", "Site key", "Pool key", 
			"Contract/FL-Code", "Title", "First Name", "Surname", "E-mail address", "Remark", 
			"Updated By", "Timestamp(SY)"};

	public byte[] exportPoolData(Long siteKey) {
		log.info("Request received to export Pool data for site {}", siteKey);
		List<ExportPoolDto> pools = getPoolsForSite(siteKey);
		
		XSSFWorkbook workbook = new XSSFWorkbook(); // Create Excel workbook
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME); // Create sheet

		// Create header row 
		createHeaderRow(workbook, sheet); //Populate data
		populateData(sheet, workbook, pools);

		try {
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH));
			workbook.write(out);
			out.close();
			log.info("PoolData.xlsx written successfully on disk.");
			workbook.close();

			return getByteArrayFromFile(FILE_PATH);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}

		return null;
	}
	
	public List<ExportPoolDto> getPoolsForSite(Long siteId) {
		log.info("fetching list of pools for site id {}", siteId);
		List<ExportPoolDto> listPool = new ArrayList<ExportPoolDto>();
		Site siteData = siteRepo.findActiveById(siteId);
		
		if(siteData != null) {
			String clusterKey = siteData.getClusterItem().getName();
			Set<SitePool> sitepoolDatadb = siteData.getSitePool();
			sitepoolDatadb.stream().forEach(sitepoolvalue -> {
				Pool poolvalue = sitepoolvalue.getPool();
				ExportPoolDto poolDto = new ExportPoolDto();
				if (poolvalue.getActive().intValue() != 0) {
					poolDto.setClusterKey(clusterKey);
					poolDto.setSiteKey(siteData.getName());
					poolDto.setPoolKey(poolvalue.getName());
					poolDto.setContract(poolvalue.getContract());
					poolDto.setRemark(poolvalue.getDescription());
					poolDto.setFirstName(poolvalue.getResponsibleFirstName());
					poolDto.setSurname(poolvalue.getResponsibleSurname());
					poolDto.setEmail(poolvalue.getResponsibleEmail());
					poolDto.setTitle(poolvalue.getResponsibleTitle());
					poolDto.setUserStamp(poolvalue.getLogUpdatedBy() != null ? poolvalue.getLogUpdatedBy() : poolvalue.getLogCreatedBy());
					poolDto.setTimeStamp(poolvalue.getLogUpdatedOn() != null ? poolvalue.getLogUpdatedOn() : poolvalue.getLogCreatedOn());
					listPool.add(poolDto);
				} 
			});
		}
		
		return listPool;
	}
	
	public Page<ExportPoolDto> getPoolData(Long siteId, Pageable pagaeble) {
		log.info("fetching list of pools for site id {}", siteId);
		//Site site = siteRepo.findById(siteId).get();
		Page<SitePool> sitePoolData = sitePoolRepo.findBySite(siteId, pagaeble);
		return sitePoolData.map(this::convertToDto);
	}
	
	private ExportPoolDto convertToDto(SitePool sitePool) {
		Pool poolValue = sitePool.getPool();
		Site siteValue = sitePool.getSite();
		ExportPoolDto poolDto = null;
		if (poolValue.getActive().intValue() != 0) {
			poolDto = new ExportPoolDto();
			poolDto.setClusterKey(siteValue.getClusterItem().getName());
			poolDto.setSiteKey(siteValue.getName());
			poolDto.setPoolKey(poolValue.getName());
			poolDto.setContract(poolValue.getContract());
			poolDto.setRemark(poolValue.getDescription());
			poolDto.setFirstName(poolValue.getResponsibleFirstName());
			poolDto.setSurname(poolValue.getResponsibleSurname());
			poolDto.setEmail(poolValue.getResponsibleEmail());
			poolDto.setTitle(poolValue.getResponsibleTitle());
			/*
			 * poolDto.setLogCreatedBy(poolValue.getLogCreatedBy());
			 * poolDto.setLogCreatedOn(poolValue.getLogCreatedOn());
			 * poolDto.setLogUpdatedBy(poolValue.getLogUpdatedBy());
			 * poolDto.setLogUpdatedOn(poolValue.getLogUpdatedOn());
			 */
			poolDto.setUserStamp(poolValue.getLogUpdatedBy() != null ? poolValue.getLogUpdatedBy() : poolValue.getLogCreatedBy());
			poolDto.setTimeStamp(poolValue.getLogUpdatedOn() != null ? poolValue.getLogUpdatedOn() : poolValue.getLogCreatedOn());
		}
		return poolDto;
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

	private void populateData(XSSFSheet sheet, XSSFWorkbook workbook, List<ExportPoolDto> pools) {
		int rowNum = 1;

		// Create a cell style to accommodate date format 
		CreationHelper helper = workbook.getCreationHelper();
		CellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(helper.createDataFormat().getFormat(DATE_FORMAT));

		for (ExportPoolDto pool : pools) {

			XSSFRow dataRow = sheet.createRow(rowNum);
			XSSFCell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(pool.getClusterKey());
			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(pool.getSiteKey());
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(pool.getPoolKey());
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(pool.getContract());
			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(pool.getTitle());
			dataCell = dataRow.createCell(5);
			dataCell.setCellValue(pool.getFirstName());
			dataCell = dataRow.createCell(6);
			dataCell.setCellValue(pool.getSurname());
			dataCell = dataRow.createCell(7);
			dataCell.setCellValue(pool.getEmail());
			dataCell = dataRow.createCell(8);
			dataCell.setCellValue(pool.getRemark());
			dataCell = dataRow.createCell(9);
			dataCell.setCellValue(pool.getUserStamp()); 
			dataCell = dataRow.createCell(10);
			dataCell.setCellValue(pool.getTimeStamp());
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
