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

import com.avaya.amsp.domain.ClusterI18NName;
import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.masterdata.dtos.ClusterDto;
import com.avaya.amsp.masterdata.repo.ClusterI18NRepository;
import com.avaya.amsp.masterdata.repo.ClusterRepository;

import lombok.extern.slf4j.Slf4j;

@Service("ExportClusterService")
@Slf4j
public class ExportClusterService {
	
	private static final String FILE_PATH = "/tmp/ClusterData.xlsx";
	private static final String SHEET_NAME = "ClusterItems";
	private static final String DATE_FORMAT = "d/m/yy h:mm";
	private static final String LANG_GERMAN = "de";
	private static final String LANG_ENGLISH = "en";
	private static final String EMPTY_STRING = "";
	
	@Autowired
	private ClusterRepository clusterRepo;
	
	@Autowired
	private ClusterI18NRepository clusterI18NRepository;
	
	private static final String[] headerColumns = new String[]{"Cluster key","Name(German)", "Name(English)","Language","Root cluster","Cluster time zone(GMT related)","Article currency",
			"Accounting currency","Cluster enabled","Orderbridge disabled","Hotline phone no.","Hotline e-mail addr.","Default cluster","SAP-TP debitor","Autom. user data synch.(default)",
			"Scout accounting enabled","Accounting e-mails(cost center head)","...with file attachment","Accounting e-mails(user)","Description of call charges: cost center level",
			"No pdf-reports abount private calls","Digits of dest. call number cover","Default PIN","Remark",
			"User stamp","Timestamp(SY)","E-mail address","E-mail addr. Cc","E-mail addr. Bcc","E-mail addr. From","Shown name from"};
	
	public byte[] exportClusterData() {
		log.info("Request received to export master Cluster data");
		List<ClusterItem> clusterItems = clusterRepo.findAll();
		
		XSSFWorkbook workbook = new XSSFWorkbook(); // Create Excel workbook
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME); //Create sheet
		
		//Create header row
		createHeaderRow(workbook, sheet);
		//Populate data
		populateData(sheet, workbook, clusterItems);
		
		try {
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH));
			workbook.write(out);
			out.close();
			log.info("ClusterData.xlsx written successfully on disk.");
			workbook.close();
			
			return getByteArrayFromFile(FILE_PATH);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}
		
		return null;
	}
	
	public Page<ClusterDto> getClusterData(Pageable pageable) {
        Page<ClusterItem> clusterItems = clusterRepo.findAll(pageable);
        return clusterItems.map(this::convertToDto);
    }
    
    
    private ClusterDto convertToDto(ClusterItem entity) {
    	ClusterDto dto = new ClusterDto();
        dto.setName(entity.getName());
        dto.setRemark(entity.getRemark());
        dto.setPinDefault(entity.getPinDefault());
        dto.setAccCurrency(entity.getAccCurrencyId().getName());
        dto.setCountry(entity.getCountry().getName());
        dto.setArticleCurrency(entity.getArticleCurrency().getName());
        dto.setHotlineEmail(entity.getHotlineEmail());
        dto.setHotlinePhone(entity.getHotlinePhone());
        dto.setLanguage(entity.getLanguage().getName());
        dto.setTimeZoneId(entity.getTimezoneId());
		/*
		 * dto.setLogCreatedBy(entity.getLogCreatedBy());
		 * dto.setLogCreatedOn(entity.getLogCreatedOn());
		 * dto.setLogUpdatedBy(entity.getLogUpdatedBy());
		 * dto.setLogUpdatedOn(entity.getLogUpdatedOn());
		 */
        dto.setUserStamp(entity.getLogUpdatedBy() != null ? entity.getLogUpdatedBy() : entity.getLogCreatedBy());
        dto.setTimeStamp(entity.getLogUpdatedOn() != null ? entity.getLogUpdatedOn() : entity.getLogCreatedOn());
        
        for(ClusterI18NName translation : entity.getClusterI18NName()) {
			if(translation.getLanguage().getId().equals(LANG_GERMAN)) {
				dto.setNameGerman(translation.getTranslation());
			} else if(translation.getLanguage().getId().equals(LANG_ENGLISH)) {
				dto.setNameEnglish(translation.getTranslation());
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
		int lastColumnNum = 30;
		sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, lastColumnNum));
		for(int i = 0; i < 31; i++) {
			XSSFCell headerCell = headerRow.createCell(i);
			headerCell.setCellValue(headerColumns[i]);
			headerCell.setCellStyle(boldStyle);
		}
	}
	
	private void populateData(XSSFSheet sheet, XSSFWorkbook workbook, List<ClusterItem> clusterItems) {
		int rowNum = 1;
		
		//Create a cell style to accommodate date format
		CreationHelper helper = workbook.getCreationHelper();
		CellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(helper.createDataFormat().getFormat(DATE_FORMAT));
		
		List<Long> clusterIds = new ArrayList<>();
		for(ClusterItem clusterItem : clusterItems) {
			clusterIds.add(clusterItem.getId());
		}
		
		//We will do one DB call and get all the i18n info in one go and process the data further instead of hitting the DB multiple times.
		List<ClusterI18NName> i18nNames = clusterI18NRepository.findByClusterIds(clusterIds); //This list contains the internationalized translated data for German and English
		List<ClusterI18NName> mapEntries;
		Map<Long, List<ClusterI18NName>> clusterLangMap = new HashMap<>(); //A given articleId will have two translations in this map
		
		for(ClusterI18NName i18nName : i18nNames) {
			mapEntries = new ArrayList<>();
			if(clusterLangMap.containsKey(i18nName.getClusterItemId())) {
				clusterLangMap.get(i18nName.getClusterItemId()).add(i18nName);
			} else {
				mapEntries.add(i18nName);
				clusterLangMap.put(i18nName.getClusterItemId(), mapEntries);
			}
		}
		
		String clusterI18nNameGerman = EMPTY_STRING;
		String clusterI18nNameEnglish = EMPTY_STRING;
		
		for(ClusterItem clusterItem : clusterItems) {
			clusterI18nNameGerman = EMPTY_STRING;
			clusterI18nNameEnglish = EMPTY_STRING;
			
			if(clusterLangMap.get(clusterItem.getId()) != null && !clusterLangMap.get(clusterItem.getId()).isEmpty()) {
				List<ClusterI18NName> listTranslations = clusterLangMap.get(clusterItem.getId());
				for(ClusterI18NName translation : listTranslations) {
					if(translation.getLanguage().getId().equals(LANG_GERMAN)) {
						clusterI18nNameGerman = translation.getTranslation();
					} else if(translation.getLanguage().getId().equals(LANG_ENGLISH)) {
						clusterI18nNameEnglish = translation.getTranslation();
					}
				}
			}
			
			XSSFRow dataRow = sheet.createRow(rowNum);
			XSSFCell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(clusterItem.getName());
			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(clusterI18nNameGerman);
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(clusterI18nNameEnglish);
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(clusterItem.getLanguage() != null ? clusterItem.getLanguage().getName() : EMPTY_STRING);
			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(5);
			dataCell.setCellValue(clusterItem.getTimezoneId());
			dataCell = dataRow.createCell(6);
			dataCell.setCellValue(clusterItem.getArticleCurrency() != null ? clusterItem.getArticleCurrency().getName() : EMPTY_STRING);
			dataCell = dataRow.createCell(7);
			dataCell.setCellValue(clusterItem.getAccCurrencyId() != null ? clusterItem.getAccCurrencyId().getName() : EMPTY_STRING);
			dataCell = dataRow.createCell(8);
			dataCell.setCellValue(clusterItem.getActive());
			dataCell = dataRow.createCell(9);
			dataCell.setCellValue(EMPTY_STRING); //TODO: Populate empty fields
			dataCell = dataRow.createCell(10);
			dataCell.setCellValue(clusterItem.getHotlinePhone());
			dataCell = dataRow.createCell(11);
			dataCell.setCellValue(clusterItem.getHotlineEmail());
			dataCell = dataRow.createCell(12);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(13);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(14);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(15);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(16);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(17);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(18);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(19);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(20);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(21);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(22);
			dataCell.setCellValue(clusterItem.getPinDefault());
			dataCell = dataRow.createCell(23);
			dataCell.setCellValue(clusterItem.getRemark());
			dataCell = dataRow.createCell(24);
			dataCell.setCellValue(clusterItem.getLogUpdatedBy() != null ? clusterItem.getLogUpdatedBy() : clusterItem.getLogCreatedBy());
			dataCell = dataRow.createCell(25);
			dataCell.setCellValue(clusterItem.getLogUpdatedOn() != null ? clusterItem.getLogUpdatedOn() : clusterItem.getLogCreatedOn());
			dataCell.setCellStyle(dateStyle);
			
			dataCell = dataRow.createCell(26);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(27);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(28);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(29);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(30);
			dataCell.setCellValue(EMPTY_STRING);
			
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
