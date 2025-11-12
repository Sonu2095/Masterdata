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

import com.avaya.amsp.domain.Article;
import com.avaya.amsp.domain.ArticleCluster;
import com.avaya.amsp.domain.ArticleI18NName;
import com.avaya.amsp.masterdata.dtos.ArticleDto;
import com.avaya.amsp.masterdata.dtos.ArticleToClusterDto;
import com.avaya.amsp.masterdata.repo.ArticleClusterRepository;
import com.avaya.amsp.masterdata.repo.ArticleI18NRepository;
import com.avaya.amsp.masterdata.repo.ArticleRepository;

import lombok.extern.slf4j.Slf4j;

@Service("ExportArticleService")
@Slf4j
public class ExportArticleService {
	
	@Autowired
	private ArticleRepository articleRepo;
	
	@Autowired
	private ArticleClusterRepository articleClusterRepo;
	
	@Autowired
	private ArticleI18NRepository articleI18nRepo;
	
	private static final String FILE_PATH_ART = "/tmp/ArticleData.xlsx";
	private static final String FILE_PATH_ART_CL = "/tmp/ArticleDataByCluster.xlsx";
	private static final String SHEET_NAME = "MasterArticles";
	private static final String DATE_FORMAT = "d/m/yy h:mm";
	private static final String LANG_GERMAN = "de";
	private static final String LANG_ENGLISH = "en";
	private static final String EMPTY_STRING = "";
	
	
	private static final String[] headerColumns = new String[]{"Article Key","Article Name(German)","Article Name(English)","Description","Abbreviation","Purchase Price Dollar","Sales Price Dollar",
			"Purchase price Euro","Sales price Euro","AVAYA SAP no.","BOSCH SAP no.","Life-time (in days)",
			"Service code","Scout key","ARTID","Clearing type","Hardware from AVAYA","Subject to authorization","Billing","Art. form","Article category","Single article",
			"At new connection","At change/move","At delete","Prio.","Planned time in min.","SLA","Wizard handler","Matching key","Default value","Read-only value",
			"Incident article","ServUs interface","Hidden","Non-available","Quantifier","Shipping address relevant","Assembling address relevant","For pool handling enabled",
			"Available for roles","User stamp","Timestamp(SY)"};
	
	public byte[] exportArticleData() {
		log.info("Request received to export master Article data");
		List<Article> articles = articleRepo.findAll();
		
		XSSFWorkbook workbook = new XSSFWorkbook(); // Create Excel workbook
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME); //Create sheet
		
	    //Create header row
		createHeaderRow(workbook, sheet);
		//Populate data
		populateData(sheet, workbook, articles);
		
		try {
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH_ART));
			workbook.write(out);
			out.close();
			log.info("ArticleData.xlsx written successfully on disk.");
			workbook.close();
			
			return getByteArrayFromFile(FILE_PATH_ART);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}
		
		return null;
	}
	
	public Page<ArticleDto> getArticleData(Pageable pageable) {
		Page<Article> articles = articleRepo.findAll(pageable);
		return articles.map(this::convertToDto);
	}
	
	private ArticleDto convertToDto(Article entity) {
		ArticleDto dto = new ArticleDto();
		dto.setName(entity.getName());
		dto.setRemark(entity.getDescription());
		dto.setPricePurchase_dollar(entity.getPricePurchaseDollar());
		dto.setPriceSales_dollar(entity.getPriceSalesDollar());
		dto.setPricePurchase_euro(entity.getPricePurchaseEuro());
		dto.setPriceSales_euro(entity.getPriceSalesEuro());
		dto.setSapAvaya(entity.getSapAvaya());
		dto.setSapBosh(entity.getSapBosh());
		dto.setLifeTime(entity.getLifeTime());
		dto.setServiceCode(entity.getServiceCode() != null ? entity.getServiceCode().getServiceCode() : EMPTY_STRING);
		dto.setArticleClearingType(entity.getArticleClearingType());
		dto.setHardwareFromAvaya(entity.getHardwareFromAvaya());
		dto.setSubjectToAuthorization(entity.getSubjectToAuthorization());
		dto.setBilling(entity.getBilling());
		dto.setArticleCategory(entity.getArticleCategory() != null ? entity.getArticleCategory().getName() : EMPTY_STRING);
		dto.setSingleArticle(entity.getSingleArticle());
		dto.setClearingAtChangeMove(entity.getClearingAtChangeMove());
		dto.setClearingAtDelete(entity.getClearingAtDelete());
		dto.setClearingAtNewConnection(entity.getClearingAtNewConnection());
		dto.setPriority(entity.getPriority());
		String sla = entity.getSlaDays() + "d " + entity.getSlaHours() + "h " + entity.getSlaMinutes() + "m";
		dto.setSla(sla);
		/*
		 * dto.setSlaDays(entity.getSlaDays()); dto.setSlaHrs(entity.getSlaHours());
		 * dto.setSlaMin(entity.getSlaMinutes());
		 */
		dto.setValueDefault(entity.getValueDefault());
		dto.setValueReadOnly(entity.getValueReadOnly() != null ? entity.getValueReadOnly().booleanValue() : false);
		dto.setIncidentArticle(entity.getIncidentArticle());
		dto.setServusInterface(entity.getServusInterface());
		dto.setHidden(entity.getHidden());
		dto.setNonAvailable(entity.getNonAvailable());
		dto.setQuantifier(entity.getQuantifier());
		dto.setShippingAddress(entity.getShippingAddress());
		dto.setAssemblingAddress(entity.getAssemblingAddress());
		dto.setPoolHandling(entity.getPoolHandling());
		dto.setArticleWizard(entity.getArticleWizardType() != null ? entity.getArticleWizardType().getName() : EMPTY_STRING);
		dto.setId(entity.getId());
		/*
		 * dto.setLogUpdatedBy(entity.getLogUpdatedBy());
		 * dto.setLogUpdatedOn(entity.getLogUpdatedOn());
		 * dto.setLogCreatedBy(entity.getLogCreatedBy());
		 * dto.setLogCreatedOn(entity.getLogCreatedOn());
		 */
		dto.setUserStamp(entity.getLogUpdatedBy() != null ? entity.getLogUpdatedBy() : entity.getLogCreatedBy());
		dto.setTimeStamp(entity.getLogUpdatedOn() != null ? entity.getLogUpdatedOn() : entity.getLogCreatedOn());
		
		for(ArticleI18NName art18nName : entity.getArticleI18NName()) {
        	if(art18nName.getLanguageId().equals("de")) {
        		dto.setNameGerman(art18nName.getTranslation());
        	} else if(art18nName.getLanguageId().equals("en")) {
        		dto.setNameEnglish(art18nName.getTranslation());
        	}
		}
		
        return dto;
    }
	
	public byte[] exportArticleDataByCluster(String clusterName) {
		log.info("Request received to export master Article data by cluster Name: " + clusterName);
		List<ArticleCluster> articles = articleClusterRepo.findByClusterName(clusterName);
		
		XSSFWorkbook workbook = new XSSFWorkbook(); // Create Excel workbook
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME); //Create sheet
		
	    //Create header row
		createHeaderRow(workbook, sheet);
		//Populate data
		populateDataByCluster(sheet, workbook, articles);
		
		try {
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH_ART_CL));
			workbook.write(out);
			out.close();
			log.info("ArticleDataByCluster.xlsx written successfully on disk.");
			workbook.close();
			
			return getByteArrayFromFile(FILE_PATH_ART_CL);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}
		
		return null;
	}
	
	public Page<ArticleToClusterDto> getArticleDataByCluster(String clusterName, Pageable pageable) {
		Page<ArticleCluster> articles = articleClusterRepo.findByClusterName(clusterName, pageable);
		return articles.map(this::convertToDto);
	}
	
	private ArticleToClusterDto convertToDto(ArticleCluster entity) {
		ArticleToClusterDto dto = new ArticleToClusterDto();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setRemark(entity.getDescription());
		dto.setPricePurchase_dollar(entity.getPricePurchaseDollar());
		dto.setPriceSales_dollar(entity.getPriceSalesDollar());
		dto.setPricePurchase_euro(entity.getPricePurchaseEuro());
		dto.setPriceSales_euro(entity.getPriceSalesEuro());
		dto.setSapAvaya(entity.getSapAvaya());
		dto.setSapBosh(entity.getSapBosh());
		dto.setLifeTime(entity.getLifeTime());
		dto.setServiceCode(entity.getServiceCodeCluster() != null ? entity.getServiceCodeCluster().getServiceCode() : EMPTY_STRING);
		dto.setArticleClearingType(entity.getArticleClearingType());
		dto.setHardwareFromAvaya(entity.getHardwareFromAvaya());
		dto.setSubjectToAuthorization(entity.getSubjectToAuthorization());
		dto.setBilling(entity.getBilling());
		dto.setArticleCategory(entity.getArticleCategory() != null ? entity.getArticleCategory().getName() : EMPTY_STRING);
		dto.setSingleArticle(entity.getSingleArticle());
		dto.setClearingAtChangeMove(entity.getClearingAtChangeMove());
		dto.setClearingAtDelete(entity.getClearingAtDelete());
		dto.setClearingAtNewConnection(entity.getClearingAtNewConnection());
		dto.setPriority(entity.getPriority());
		/*
		 * dto.setSlaDays(entity.getSlaDays()); dto.setSlaHrs(entity.getSlaHours());
		 * dto.setSlaMin(entity.getSlaMinutes());
		 */
		String sla = entity.getSlaDays() + "d " + entity.getSlaHours() + "h " + entity.getSlaMinutes() + "m";
		dto.setSla(sla);
		dto.setValueDefault(entity.getValueDefault());
		dto.setValueReadOnly(entity.getValueReadOnly() != null ? entity.getValueReadOnly().booleanValue() : false);
		dto.setIncidentArticle(entity.getIncidentArticle());
		dto.setServusInterface(entity.getServusInterface() != null ? entity.getServusInterface().intValue() : 0);
		dto.setHidden(entity.getHidden());
		dto.setNonAvailable(entity.getNonAvailable());
		dto.setQuantifier(entity.getQuantifier());
		dto.setShippingAddress(entity.getShippingAddress());
		dto.setAssemblingAddress(entity.getAssemblingAddress());
		dto.setPoolHandling(entity.getPoolHandling());
		dto.setArticleWizard(entity.getArticleWizardType() != null ? entity.getArticleWizardType().getName() : EMPTY_STRING);
		dto.setId(entity.getId());
		/*
		 * dto.setLogUpdatedBy(entity.getLogUpdatedBy());
		 * dto.setLogUpdatedOn(entity.getLogUpdatedOn());
		 * dto.setLogCreatedBy(entity.getLogCreatedBy());
		 * dto.setLogCreatedOn(entity.getLogCreatedOn());
		 */
		dto.setUserStamp(entity.getLogUpdatedBy() != null ? entity.getLogUpdatedBy() : entity.getLogCreatedBy());
		dto.setTimeStamp(entity.getLogUpdatedOn() != null ? entity.getLogUpdatedOn() : entity.getLogCreatedOn());
		
		for(ArticleI18NName art18nName : entity.getArticle().getArticleI18NName()) {
        	if(art18nName.getLanguageId().equals("de")) {
        		dto.setNameGerman(art18nName.getTranslation());
        	} else if(art18nName.getLanguageId().equals("en")) {
        		dto.setNameEnglish(art18nName.getTranslation());
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
		int lastColumnNum = 42;
		sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, lastColumnNum));
		for(int i = 0; i < 43; i++) {
			XSSFCell headerCell = headerRow.createCell(i);
			headerCell.setCellValue(headerColumns[i]);
			headerCell.setCellStyle(boldStyle);
		}
	}
	
	private void populateData(XSSFSheet sheet, XSSFWorkbook workbook, List<Article> articles) {
		int rowNum = 1;
		
		//Create a cell style to accommodate date format
		CreationHelper helper = workbook.getCreationHelper();
		CellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(helper.createDataFormat().getFormat(DATE_FORMAT));
		
		List<Long> articleIds = new ArrayList<>();
		for(Article article : articles) {
			articleIds.add(article.getId());
		}
		
		//We will do one DB call and get all the i18n info in one go and process the data further instead of hitting the DB multiple times.
		List<ArticleI18NName> i18nNames = articleI18nRepo.findByArticleIds(articleIds); //This list contains the internationalized translated data for German and English
		List<ArticleI18NName> mapEntries;
		Map<Long, List<ArticleI18NName>> articleLangMap = new HashMap<>(); //A given articleId will have two translations in this map
		
		for(ArticleI18NName i18nName : i18nNames) {
			mapEntries = new ArrayList<>();
			if(articleLangMap.containsKey(i18nName.getArticleId())) {
				articleLangMap.get(i18nName.getArticleId()).add(i18nName);
			} else {
				mapEntries.add(i18nName);
				articleLangMap.put(i18nName.getArticleId(), mapEntries);
			}
		}
		
		String articleI18nNameGerman = EMPTY_STRING;
		String articleI18nNameEnglish = EMPTY_STRING;
		
		for(Article article : articles) {
			XSSFRow dataRow = sheet.createRow(rowNum);
			XSSFCell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(article.getName());

			articleI18nNameGerman = EMPTY_STRING;
			articleI18nNameEnglish = EMPTY_STRING;
			
			if(articleLangMap.get(article.getId()) != null && !articleLangMap.get(article.getId()).isEmpty()) {
				List<ArticleI18NName> listTranslations = articleLangMap.get(article.getId());
				for(ArticleI18NName translation : listTranslations) {
					if(translation.getLanguage().getId().equals(LANG_GERMAN)) {
						articleI18nNameGerman = translation.getTranslation();
					} else if(translation.getLanguage().getId().equals(LANG_ENGLISH)) {
						articleI18nNameEnglish = translation.getTranslation();
					}
				}
			}
			
			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(articleI18nNameGerman);
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(articleI18nNameEnglish);
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(article.getDescription());
			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(5);
			dataCell.setCellValue(article.getPricePurchaseDollar());
			dataCell = dataRow.createCell(6);
			dataCell.setCellValue(article.getPriceSalesDollar());
			dataCell = dataRow.createCell(7);
			dataCell.setCellValue(article.getPricePurchaseEuro());
			dataCell = dataRow.createCell(8);
			dataCell.setCellValue(article.getPriceSalesEuro());
			dataCell = dataRow.createCell(9);
			dataCell.setCellValue(article.getSapAvaya());
			dataCell = dataRow.createCell(10);
			dataCell.setCellValue(article.getSapBosh());
			dataCell = dataRow.createCell(11);
			dataCell.setCellValue(article.getLifeTime());
			dataCell = dataRow.createCell(12);
			dataCell.setCellValue(article.getServiceCode() != null ? article.getServiceCode().getServiceCode() : EMPTY_STRING);
			dataCell = dataRow.createCell(13);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(14);
			dataCell.setCellValue(article.getId());
			dataCell = dataRow.createCell(15);
			dataCell.setCellValue(article.getArticleClearingType() != null ? article.getArticleClearingType().name() : EMPTY_STRING);
			dataCell = dataRow.createCell(16);
			dataCell.setCellValue(article.getHardwareFromAvaya());
			dataCell = dataRow.createCell(17);
			dataCell.setCellValue(article.getSubjectToAuthorization());
			dataCell = dataRow.createCell(18);
			dataCell.setCellValue(article.getBilling());
			dataCell = dataRow.createCell(19);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(20);
			dataCell.setCellValue(article.getArticleCategory() != null ? article.getArticleCategory().getName() : EMPTY_STRING);
			dataCell = dataRow.createCell(21);
			dataCell.setCellValue(article.getSingleArticle() != null ? article.getSingleArticle().booleanValue() : false); //TODO: Has it to be defaulted to false?
			dataCell = dataRow.createCell(22);
			dataCell.setCellValue(article.getClearingAtNewConnection());
			dataCell = dataRow.createCell(23);
			dataCell.setCellValue(article.getClearingAtChangeMove());
			dataCell = dataRow.createCell(24);
			dataCell.setCellValue(article.getClearingAtDelete());
			dataCell = dataRow.createCell(25);
			dataCell.setCellValue(article.getPriority());
			dataCell = dataRow.createCell(26);
			dataCell.setCellValue(EMPTY_STRING);
			String sla = article.getSlaDays() + "d " + article.getSlaHours() + "h " + article.getSlaMinutes() + "m";
			dataCell = dataRow.createCell(27);
			dataCell.setCellValue(sla);
			dataCell = dataRow.createCell(28);
			dataCell.setCellValue(article.getArticleWizardType() != null ? article.getArticleWizardType().getName() : EMPTY_STRING);
			dataCell = dataRow.createCell(29);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(30);
			dataCell.setCellValue(article.getValueDefault());
			dataCell = dataRow.createCell(31);
			dataCell.setCellValue(article.getValueReadOnly() != null ? article.getValueReadOnly().booleanValue() : false); //TODO: Has it to be defaulted to false?
			dataCell = dataRow.createCell(32);
			dataCell.setCellValue(article.getIncidentArticle());
			dataCell = dataRow.createCell(33);
			dataCell.setCellValue(article.getServusInterface());
			dataCell = dataRow.createCell(34);
			dataCell.setCellValue(article.getHidden());
			dataCell = dataRow.createCell(35);
			dataCell.setCellValue(article.getNonAvailable());
			dataCell = dataRow.createCell(36);
			dataCell.setCellValue(article.getQuantifier());
			dataCell = dataRow.createCell(37);
			dataCell.setCellValue(article.getShippingAddress());
			dataCell = dataRow.createCell(38);
			dataCell.setCellValue(article.getAssemblingAddress());
			dataCell = dataRow.createCell(39);
			dataCell.setCellValue(article.getPoolHandling());
			dataCell = dataRow.createCell(40);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(41);
			dataCell.setCellValue(article.getLogUpdatedBy() != null ? article.getLogUpdatedBy() : article.getLogCreatedBy());
			dataCell = dataRow.createCell(42);
			dataCell.setCellValue(article.getLogUpdatedOn() != null ? article.getLogUpdatedOn() : article.getLogCreatedOn());
			dataCell.setCellStyle(dateStyle);
			
			rowNum++;
		}
		
		/*"Article Key","Article Name(German)","Article Name(English)","Description","Abbreviation","Purchase Price Dollar","Sales Price Dollar",
		"Purchase price Euro","Sales price Euro","AVAYA SAP no.","BOSCH SAP no.","Life-time (in days)",
		"Lasl number","Scout key","ARTID","Clearing type","Hardware from AVAYA","Subject to authorization","Billing","Art. form","Article category","Single article",
		"At new connection","At change/move","At delete","Prio.","Planned time in min.","SLA","Wizard handler","Matching key","Default value","Read-only value",
		"Incident article","ServUs interface","Hidden","Non-available","Quantifier","Shipping address relevant","Assembling address relevant","For pool handling enabled",
		"Available for roles","User stamp","Timestamp(SY)"*/
	}
	
	private void populateDataByCluster(XSSFSheet sheet, XSSFWorkbook workbook, List<ArticleCluster> articles) {
		int rowNum = 1;
		
		//Create a cell style to accommodate date format
		CreationHelper helper = workbook.getCreationHelper();
		CellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(helper.createDataFormat().getFormat(DATE_FORMAT));
		
		List<Long> articleIds = new ArrayList<>();
		for(ArticleCluster article : articles) {
			articleIds.add(article.getId());
		}
		
		//We will do one DB call and get all the i18n info in one go and process the data further instead of hitting the DB multiple times.
		List<ArticleI18NName> i18nNames = articleI18nRepo.findByArticleIds(articleIds); //This list contains the internationalized translated data for German and English
		List<ArticleI18NName> mapEntries;
		Map<Long, List<ArticleI18NName>> articleLangMap = new HashMap<>(); //A given articleId will have two translations in this map
		
		for(ArticleI18NName i18nName : i18nNames) {
			mapEntries = new ArrayList<>();
			if(articleLangMap.containsKey(i18nName.getArticleId())) {
				articleLangMap.get(i18nName.getArticleId()).add(i18nName);
			} else {
				mapEntries.add(i18nName);
				articleLangMap.put(i18nName.getArticleId(), mapEntries);
			}
		}
		
		String articleI18nNameGerman = EMPTY_STRING;
		String articleI18nNameEnglish = EMPTY_STRING;
		
		for(ArticleCluster article : articles) {
			XSSFRow dataRow = sheet.createRow(rowNum);
			XSSFCell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(article.getName());

			articleI18nNameGerman = EMPTY_STRING;
			articleI18nNameEnglish = EMPTY_STRING;
			
			if(articleLangMap.get(article.getId()) != null && !articleLangMap.get(article.getId()).isEmpty()) {
				List<ArticleI18NName> listTranslations = articleLangMap.get(article.getId());
				for(ArticleI18NName translation : listTranslations) {
					if(translation.getLanguage().getId().equals(LANG_GERMAN)) {
						articleI18nNameGerman = translation.getTranslation();
					} else if(translation.getLanguage().getId().equals(LANG_ENGLISH)) {
						articleI18nNameEnglish = translation.getTranslation();
					}
				}
			}
			
			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(articleI18nNameGerman);
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(articleI18nNameEnglish);
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(article.getDescription());
			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(5);
			dataCell.setCellValue(article.getPricePurchaseDollar());
			dataCell = dataRow.createCell(6);
			dataCell.setCellValue(article.getPriceSalesDollar());
			dataCell = dataRow.createCell(7);
			dataCell.setCellValue(article.getPricePurchaseEuro());
			dataCell = dataRow.createCell(8);
			dataCell.setCellValue(article.getPriceSalesEuro());
			dataCell = dataRow.createCell(9);
			dataCell.setCellValue(article.getSapAvaya());
			dataCell = dataRow.createCell(10);
			dataCell.setCellValue(article.getSapBosh());
			dataCell = dataRow.createCell(11);
			dataCell.setCellValue(article.getLifeTime());
			dataCell = dataRow.createCell(12);
			dataCell.setCellValue(article.getServiceCodeCluster() != null ? article.getServiceCodeCluster().getServiceCode() : EMPTY_STRING);
			dataCell = dataRow.createCell(13);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(14);
			dataCell.setCellValue(article.getId());
			dataCell = dataRow.createCell(15);
			dataCell.setCellValue(article.getArticleClearingType() != null ? article.getArticleClearingType().name() : EMPTY_STRING);
			dataCell = dataRow.createCell(16);
			dataCell.setCellValue(article.getHardwareFromAvaya());
			dataCell = dataRow.createCell(17);
			dataCell.setCellValue(article.getSubjectToAuthorization());
			dataCell = dataRow.createCell(18);
			dataCell.setCellValue(article.getBilling());
			dataCell = dataRow.createCell(19);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(20);
			dataCell.setCellValue(article.getArticleCategory() != null ? article.getArticleCategory().getName() : EMPTY_STRING);
			dataCell = dataRow.createCell(21);
			dataCell.setCellValue(article.getSingleArticle() != null ? article.getSingleArticle().booleanValue() : false); //TODO: Has it to be defaulted to false?
			dataCell = dataRow.createCell(22);
			dataCell.setCellValue(article.getClearingAtNewConnection());
			dataCell = dataRow.createCell(23);
			dataCell.setCellValue(article.getClearingAtChangeMove());
			dataCell = dataRow.createCell(24);
			dataCell.setCellValue(article.getClearingAtDelete());
			dataCell = dataRow.createCell(25);
			dataCell.setCellValue(article.getPriority());
			dataCell = dataRow.createCell(26);
			dataCell.setCellValue(EMPTY_STRING);
			String sla = article.getSlaDays() + "d " + article.getSlaHours() + "h " + article.getSlaMinutes() + "m";
			dataCell = dataRow.createCell(27);
			dataCell.setCellValue(sla);
			dataCell = dataRow.createCell(28);
			dataCell.setCellValue(article.getArticleWizardType() != null ? article.getArticleWizardType().getName() : EMPTY_STRING);
			dataCell = dataRow.createCell(29);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(30);
			dataCell.setCellValue(article.getValueDefault());
			dataCell = dataRow.createCell(31);
			dataCell.setCellValue(article.getValueReadOnly() != null ? article.getValueReadOnly().booleanValue() : false); //TODO: Has it to be defaulted to false?
			dataCell = dataRow.createCell(32);
			dataCell.setCellValue(article.getIncidentArticle());
			dataCell = dataRow.createCell(33);
			dataCell.setCellValue(article.getServusInterface() != null ? article.getServusInterface().intValue() : 0);
			dataCell = dataRow.createCell(34);
			dataCell.setCellValue(article.getHidden());
			dataCell = dataRow.createCell(35);
			dataCell.setCellValue(article.getNonAvailable());
			dataCell = dataRow.createCell(36);
			dataCell.setCellValue(article.getQuantifier());
			dataCell = dataRow.createCell(37);
			dataCell.setCellValue(article.getShippingAddress());
			dataCell = dataRow.createCell(38);
			dataCell.setCellValue(article.getAssemblingAddress());
			dataCell = dataRow.createCell(39);
			dataCell.setCellValue(article.getPoolHandling());
			dataCell = dataRow.createCell(40);
			dataCell.setCellValue(EMPTY_STRING);
			dataCell = dataRow.createCell(41);
			dataCell.setCellValue(article.getLogUpdatedBy() != null ? article.getLogUpdatedBy() : article.getLogCreatedBy());
			dataCell = dataRow.createCell(42);
			dataCell.setCellValue(article.getLogUpdatedOn() != null ? article.getLogUpdatedOn() : article.getLogCreatedOn());
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
