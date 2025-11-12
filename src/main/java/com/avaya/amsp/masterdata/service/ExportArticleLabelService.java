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
import com.avaya.amsp.domain.ArticleI18NName;
import com.avaya.amsp.masterdata.dtos.ArticleLabelDto;
import com.avaya.amsp.masterdata.repo.ArticleI18NRepository;
import com.avaya.amsp.masterdata.repo.ArticleRepository;

import lombok.extern.slf4j.Slf4j;

@Service("ExportArticleLabelService")
@Slf4j
public class ExportArticleLabelService {
	
	@Autowired
	private ArticleRepository articleRepo;
	
	@Autowired
	private ArticleI18NRepository articleI18nRepo;
	
	private static final String FILE_PATH_ART = "/tmp/ArticleLabelData.xlsx";
	private static final String SHEET_NAME = "ArticleLabel";
	private static final String DATE_FORMAT = "d/m/yy h:mm";
	private static final String EMPTY_STRING = "";
	private static final String LANG_GERMAN = "de";
	private static final String LANG_ENGLISH = "en";
	
	Map<Long, List<ArticleI18NName>> articleLangMap = new HashMap<>(); 
	
	private static final String[] headerColumns = new String[]{"Article key","Wizard handler","Name(English)","Name(German)"};
	
	public byte[] exportArticleLabel() {
		log.info("Request received to export Article Label data");
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
			log.info("ArticleLabelData.xlsx written successfully on disk.");
			workbook.close();
			
			return getByteArrayFromFile(FILE_PATH_ART);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}
		
		return null;
	}
	
	public Page<ArticleLabelDto> getArticleLabel(Pageable pageable) {
		log.info("Request received to get Article Label data");
		
		Page<Article> articlesPage = articleRepo.findAll(pageable);
		
		List<Long> articleIds = new ArrayList<>();
		for(Article article : articlesPage.getContent()) {
			articleIds.add(article.getId());
		}
		
		//We will do one DB call and get all the i18n info in one go and process the data further instead of hitting the DB multiple times.
		List<ArticleI18NName> i18nNames = articleI18nRepo.findByArticleIds(articleIds); //This list contains the internationalized translated data for German and English
		List<ArticleI18NName> mapEntries;
		//Map<Long, List<ArticleI18NName>> articleLangMap = new HashMap<>(); //A given articleId will have two translations in this map
		
		for(ArticleI18NName i18nName : i18nNames) {
			mapEntries = new ArrayList<>();
			if(articleLangMap.containsKey(i18nName.getArticleId())) {
				articleLangMap.get(i18nName.getArticleId()).add(i18nName);
			} else {
				mapEntries.add(i18nName);
				articleLangMap.put(i18nName.getArticleId(), mapEntries);
			}
		}
		
		return articlesPage.map(this::convertToDto);
		
	}
	
	private ArticleLabelDto convertToDto(Article entity) {
		ArticleLabelDto dto = new ArticleLabelDto();
		dto.setArticleKey(entity.getName());
		dto.setWizardHandler(entity.getArticleWizardType() != null ? entity.getArticleWizardType().getName() : null);
		
		List<ArticleI18NName> listTranslations = articleLangMap.get(entity.getId());
        
		for(ArticleI18NName translation : listTranslations) {
			if(translation.getLanguage().getId().equals(LANG_GERMAN)) {
				dto.setArticleNameGerman(translation.getTranslation());
			} else if(translation.getLanguage().getId().equals(LANG_ENGLISH)) {
				dto.setArticleNameEnglish(translation.getTranslation());
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
		int lastColumnNum = 3;
		sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, lastColumnNum));
		for(int i = 0; i < 4; i++) {
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
			dataCell.setCellValue(article.getArticleWizardType() != null ? article.getArticleWizardType().getName() : EMPTY_STRING);
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(articleI18nNameEnglish);
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(articleI18nNameGerman);
			
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
