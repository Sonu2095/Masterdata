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

import com.avaya.amsp.domain.Site;
import com.avaya.amsp.domain.SiteI18NName;
import com.avaya.amsp.masterdata.dtos.SiteDto;
import com.avaya.amsp.masterdata.repo.SiteI18NRepository;
import com.avaya.amsp.masterdata.repo.SiteRepository;

import lombok.extern.slf4j.Slf4j;

@Service("ExportSiteService")
@Slf4j
public class ExportSiteService {

	private static final String FILE_PATH = "/tmp/SiteData.xlsx";
	private static final String SHEET_NAME = "Sites";
	private static final String DATE_FORMAT = "d/m/yy h:mm";
	private static final String LANG_GERMAN = "de";
	private static final String LANG_ENGLISH = "en";
	private static final String EMPTY_STRING = "";

	@Autowired
	private SiteRepository siteRepo;

	@Autowired
	private SiteI18NRepository siteI18NRepository;

	private static final String[] headerColumns = new String[] { "Cluster key", "Site key", "Location code", 
			"City", "Street", "Name(German)", "Name(English)", "Remark", "No synch. with user data", 
			"SIP Domain", "RoutingPolicyDestination", "Notes", "CM Name", "ARS Analysis Entry", "User stamp",
			"Time Stamp(SY)"};

	public byte[] exportSiteData(Long clusterKey) {
		log.info("Request received to export Site data for cluster {}", clusterKey);
		List<Site> sites = siteRepo.findByClusterItem(clusterKey);

		XSSFWorkbook workbook = new XSSFWorkbook(); // Create Excel workbook
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME); // Create sheet

		// Create header row 
		createHeaderRow(workbook, sheet); //Populate data
		populateData(sheet, workbook, sites);

		try {
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH));
			workbook.write(out);
			out.close();
			log.info("SiteData.xlsx written successfully on disk.");
			workbook.close();

			return getByteArrayFromFile(FILE_PATH);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}
		return null;
	}
	
	public Page<SiteDto> getSiteData(Long clusterKey, Pageable pageable) {
		Page<Site> sites = siteRepo.findByClusterId(clusterKey, pageable);
		return sites.map(this::convertToDto);
	}
	
	private SiteDto convertToDto(Site entity) {
		SiteDto dto = new SiteDto();
		dto.setArs(entity.getArs());
		dto.setCity(entity.getCity());
		dto.setClusterName(entity.getClusterItem().getName());
		dto.setCmName(entity.getCmName());
		dto.setLocationCode(entity.getLocationCode());
		dto.setStreet(entity.getStreet());
		dto.setSipDomain(entity.getSipDomain());
		dto.setRoutingPolicy(entity.getRoutingPolicy());
		dto.setRemark(entity.getRemark());
		dto.setNotes(entity.getNotes());
		/*
		 * dto.setLogUpdatedOn(entity.getLogUpdatedOn());
		 * dto.setLogUpdatedBy(entity.getLogUpdatedBy());
		 * dto.setLogCreatedBy(entity.getLogCreatedBy());
		 * dto.setLogCreatedOn(entity.getLogCreatedOn());
		 */
		dto.setName(entity.getName());
		dto.setUserStamp(entity.getLogUpdatedBy() != null ? entity.getLogUpdatedBy() : entity.getLogCreatedBy());
		dto.setTimeStamp(entity.getLogUpdatedOn() != null ? entity.getLogUpdatedOn() : entity.getLogCreatedOn());
		
		for(SiteI18NName site18nName : entity.getSiteI18NName()) {
        	if(site18nName.getLanguageId().equals("de")) {
        		dto.setNameGerman(site18nName.getTranslation());
        	} else if(site18nName.getLanguageId().equals("en")) {
        		dto.setNameEnglish(site18nName.getTranslation());
        	}
		}
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

	private void populateData(XSSFSheet sheet, XSSFWorkbook workbook, List<Site> sites) {
		int rowNum = 1;

		// Create a cell style to accommodate date format 
		CreationHelper helper = workbook.getCreationHelper();
		CellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(helper.createDataFormat().getFormat(DATE_FORMAT));

		List<Long> siteIds = new ArrayList<>();
		for (Site site : sites) {
			siteIds.add(site.getId());
		}

		/*
		 * We will do one DB call and get all the i18n info in one go and process the
		 * data further instead of hitting the DB multiple times.
		 */
		List<SiteI18NName> i18nNames = siteI18NRepository.findBySiteIds(siteIds); // This list contains the
																								// internationalized
																								// translated data for
																								// German and English
		List<SiteI18NName> mapEntries;
		Map<Long, List<SiteI18NName>> siteLangMap = new HashMap<>(); // A given articleId will have two
																			// translations in this map

		for (SiteI18NName i18nName : i18nNames) {
			mapEntries = new ArrayList<>();
			if (siteLangMap.containsKey(i18nName.getSiteId())) {
				siteLangMap.get(i18nName.getSiteId()).add(i18nName);
			} else {
				mapEntries.add(i18nName);
				siteLangMap.put(i18nName.getSiteId(), mapEntries);
			}
		}

		String siteI18nNameGerman = EMPTY_STRING;
		String siteI18nNameEnglish = EMPTY_STRING;

		for (Site site : sites) {
			siteI18nNameGerman = EMPTY_STRING;
			siteI18nNameEnglish = EMPTY_STRING;

			if (siteLangMap.get(site.getId()) != null && !siteLangMap.get(site.getId()).isEmpty()) {
				List<SiteI18NName> listTranslations = siteLangMap.get(site.getId());
				for (SiteI18NName translation : listTranslations) {
					if (translation.getLanguage().getId().equals(LANG_GERMAN)) {
						siteI18nNameGerman = translation.getTranslation();
					} else if (translation.getLanguage().getId().equals(LANG_ENGLISH)) {
						siteI18nNameEnglish = translation.getTranslation();
					}
				}
			}

			XSSFRow dataRow = sheet.createRow(rowNum);
			XSSFCell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(site.getClusterItem().getName());
			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(site.getName());
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(site.getLocationCode());
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(site.getCity());
			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(site.getStreet());
			dataCell = dataRow.createCell(5);
			dataCell.setCellValue(siteI18nNameGerman);
			dataCell = dataRow.createCell(6);
			dataCell.setCellValue(siteI18nNameEnglish);
			dataCell = dataRow.createCell(7);
			dataCell.setCellValue(site.getRemark());
			dataCell = dataRow.createCell(8);
			dataCell.setCellValue(EMPTY_STRING); // TODO: Populate empty fields
			dataCell = dataRow.createCell(9);
			dataCell.setCellValue(site.getSipDomain()); 
			dataCell = dataRow.createCell(10);
			dataCell.setCellValue(site.getRoutingPolicy());
			dataCell = dataRow.createCell(11);
			dataCell.setCellValue(site.getNotes());
			dataCell = dataRow.createCell(12);
			dataCell.setCellValue(site.getCmName());
			dataCell = dataRow.createCell(13);
			dataCell.setCellValue(site.getArs() != null && site.getArs().booleanValue());
			dataCell = dataRow.createCell(14);
			dataCell.setCellValue(site.getLogUpdatedBy() != null ? site.getLogUpdatedBy() : site.getLogCreatedBy());
			dataCell = dataRow.createCell(15);
			dataCell.setCellValue(site.getLogUpdatedOn() != null ? site.getLogUpdatedOn() : site.getLogCreatedOn());
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
