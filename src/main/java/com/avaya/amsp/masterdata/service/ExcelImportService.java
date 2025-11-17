package com.avaya.amsp.masterdata.service;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.avaya.amsp.domain.Article;
import com.avaya.amsp.domain.ArticleCategory;
import com.avaya.amsp.domain.ArticleClearingTypeEnum;
import com.avaya.amsp.domain.ServiceCode;
import com.avaya.amsp.masterdata.repo.ArticleRepository;
import com.avaya.amsp.masterdata.repo.ServiceCodeRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ExcelImportService {

    private final ServiceCodeRepository serviceCodeRepository;

    @Autowired
    private ArticleRepository articleRepository;
    
	
    ExcelImportService(ServiceCodeRepository serviceCodeRepository) {
        this.serviceCodeRepository = serviceCodeRepository;
    }


    public void saveDataFromExcel(MultipartFile file) {

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
        	
    		//Article articleData = new Article();
    		ArticleCategory articleCategory = new ArticleCategory();
    		ServiceCode serviceCode = new ServiceCode();
    		
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Assuming the first row contains the headers
            Row headerRow = rows.next();

            Map<String, Integer> columnMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String columnName = cell.getStringCellValue();
                columnMap.put(columnName, cell.getColumnIndex());
            }

            while (rows.hasNext()) {
                Row row = rows.next();
                Article article = new Article(); // Replace with your entity class

                for (Map.Entry<String, Integer> entry : columnMap.entrySet()) {
                    String columnName = entry.getKey().trim();
                    Cell cell = row.getCell(entry.getValue());
                    String cellValue = (cell != null) ? cell.getStringCellValue() : null; // Avoid NullPointerException

                    // Use switch case to map each column to the appropriate setter
                    switch (columnName) {
                    	
                        case "Article key":
                        	article.setName(cellValue);
                            break;
                        case "Description":
                        	article.setDescription(cellValue);
                            break;
                        case "Purchase price":
                        	if(parseDouble(cellValue) != null) 
                            	article.setPricePurchaseEuro(parseDouble(cellValue));  	
                            break;
                        case "Sales price":
                        	if(parseDouble(cellValue) != null) 
                                article.setPriceSalesEuro(parseDouble(cellValue));                        		                       	
                            break;
                        case "Purchase price(2)":
                        	if(parseDouble(cellValue) != null) 
                            	article.setPricePurchaseDollar(parseDouble(cellValue));                       	
                            break;
                        case "Sales price(2)":
                        	if(parseDouble(cellValue) != null)
                                article.setPricePurchaseDollar(parseDouble(cellValue));                        		                        	
                            break;
                        case "AVAYA SAP no.":
                            article.setSapAvaya(cellValue);
                            break;
                        case "BOSCH SAP no.":
                            article.setSapBosh(cellValue);
                            break;
						
                        case "Service code":
                    		if (cellValue == null || cellValue == "0") {
                    			article.setServiceCode(null);
                    		}
                    		else {
                    			serviceCode = serviceCodeRepository.findByServiceCode(cellValue);
                				article.setServiceCode(serviceCode);
                			}
                            break;
                        case "Clearing type":
                        	if(parseInt(cellValue) != null)
                        		article.setArticleClearingType(getClearingTypeFromValue(cellValue));
                            break;
                        case "Hardware from AVAYA":
                        	if(parseInt(cellValue) != null)
                        		article.setHardwareFromAvaya(parseInt(cellValue));
                            break;
                        case "Subject to authorization":
                        	if(parseInt(cellValue) != null)
                        		article.setSubjectToAuthorization(parseInt(cellValue));
                            break;
                        case "Billing":
                        	if(parseInt(cellValue) != null)
                        		article.setBilling(parseInt(cellValue));
                            break;
                        case "Article category":
                    		if (cellValue == null || parseLong(cellValue) == 0 ) {
                    			article.setArticleCategory(null);
                    		} else {
                    			articleCategory.setId(parseLong(cellValue));
                    			article.setArticleCategory(articleCategory);
                    		}
                            break;
                        case "Single article":
                        	if(parseInt(cellValue) != null) {
	                        	Integer val = parseInt(cellValue);
	                            article.setSingleArticle(val != 0);
                        	}
                            break;
                        case "At new connection":
                        	if(parseInt(cellValue) != null)
                            	article.setClearingAtNewConnection(parseInt(cellValue));
                            break;
                        case "At change move":
                        	if(parseInt(cellValue) != null)
                        		article.setClearingAtChangeMove(parseInt(cellValue));
                            break;
                        case "Prio.":
                        	if(parseInt(cellValue) != null)
                        		article.setPriority(parseInt(cellValue));
                        	break;
                        case "SLA days":
                        	if(parseInt(cellValue) != null)
                        		article.setSlaDays(parseInt(cellValue));
                            break;
                        case "SLA hrs":
                        	if(parseInt(cellValue) != null)
                        		article.setSlaHours(parseInt(cellValue));
                            break;
                        case "SLA min.":
                        	if(parseInt(cellValue) != null)
                        		article.setSlaMinutes(parseInt(cellValue));
                            break;
                        case "Default value":
                            article.setValueDefault(cellValue);
                            break;
                        case "Read-only value":
                        	if(parseInt(cellValue) != null) {
	                        	Integer valR = parseInt(cellValue);
	                            article.setValueReadOnly(valR != 0);
                        	}
                            break;
                        case "Incident article":
                        	if(parseInt(cellValue) != null)
                        		article.setIncidentArticle(parseInt(cellValue));
                            break;
                        case "ServUs interface":
                        	if(parseInt(cellValue) != null)
                        		article.setServusInterface(parseInt(cellValue));
                            break;
                        case "Hidden":
                        	if(parseInt(cellValue) != null)
                        		article.setHidden(parseInt(cellValue));
                            break;
                        case "Non-available":
                        	if(parseInt(cellValue) != null)
                        		article.setNonAvailable(parseInt(cellValue));
                            break;
                        case "Quantifier":
                        	if(parseInt(cellValue) != null)
                        		article.setQuantifier(parseInt(cellValue));
                            break;
                        case "Shipping address relevant":
                        	if(parseInt(cellValue) != null)
                        		article.setShippingAddress(parseInt(cellValue));
                            break;
                        case "Assembling address relevant":
                        	if(parseInt(cellValue) != null)
                        		article.setAssemblingAddress(parseInt(cellValue));
                            break;
                        case "For pool handling enabled":
                        	if(parseInt(cellValue) != null)
                        		article.setPoolHandling(parseInt(cellValue));
                            break;
                        case "User stamp":
                            article.setLogCreatedBy(cellValue);
                            break;
                        case "Time Stamp":
                            article.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
                            break;
                        case "ARTID":
                            article.setId(Long.parseLong(cellValue));
                            break;
                        default:
                            // Handle unexpected columns or ignore them
                            break;
                    }
                }
                // Check if the article with the same name already exists
                Article existingArticle = articleRepository.findByName(article.getName());

                if (existingArticle != null) {
    				log.info("Article with name {} already exists.",  article.getName());
                   
                } else {
                	article.setActive(1);
                    articleRepository.save(article); // Save new article if not found
                }     
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to process Excel file: " + e.getMessage());
        }
    }
    
    // Helper methods to safely parse values
    private Double parseDouble(String value) {
        if (value != null && !value.isEmpty()) {
            return Double.parseDouble(value);
        }
        return null; // Return null if the input is null or empty
    }

    private Integer parseInt(String value) {
        if (value != null && !value.isEmpty()) {
            return Integer.parseInt(value);
        }
        return null; // Return null if the input is null or empty
    }
    
    private Integer parseLong(String value) {
        if (value != null && !value.isEmpty()) {
            return Integer.parseInt(value);
        }
        return null; // Return null if the input is null or empty
    }
    
 // Method to map string to enum
    private ArticleClearingTypeEnum getClearingTypeFromValue(String value) {
        switch (value) {
            case "1":
                return ArticleClearingTypeEnum.ONETIME; // replace with actual enum value
            case "2":
                return ArticleClearingTypeEnum.MONTHLY; // replace with actual enum value
            // Add additional mappings as needed
            default:
                throw new IllegalArgumentException("No enum constant for value: " + value);
        }
    }
}
