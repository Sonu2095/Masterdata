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

import com.avaya.amsp.domain.CalendarDayLightSaving;
import com.avaya.amsp.masterdata.dtos.CalendarDayLightSavingDto;
import com.avaya.amsp.masterdata.repo.CalendarDayLightSavingRepository;

import lombok.extern.slf4j.Slf4j;

@Service("ExportCalendarDaylightSavingsDataService")
@Slf4j
public class ExportCalendarDaylightSavingsDataService {
	
	private static final String FILE_PATH = "/tmp/CalendarDaylightSavingsData.xlsx";
	private static final String SHEET_NAME = "CalendarDaylightSavingsData";
	private static final String DATE_FORMAT = "d/m/yy h:mm";
	
	@Autowired
	private CalendarDayLightSavingRepository calendarDayLightSavingRepository;
	
	private static final String[] headerColumns = new String[]{"Calendar key", "Dst year", "Switch to summer time", "Return to standard time", "Hour offset", "Remark"};
	
	public byte[] exportCalendarDaylightSavingData() {
		log.info("Request received to export CalendarDaylightSavingsData");
		List<CalendarDayLightSaving> listCalendarDayLightSaving = calendarDayLightSavingRepository.findAll();
		
		XSSFWorkbook workbook = new XSSFWorkbook(); // Create Excel workbook
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME); //Create sheet
		
		//Create header row
		createHeaderRow(workbook, sheet);
		//Populate data
		populateData(sheet, workbook, listCalendarDayLightSaving);
		
		try {
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH));
			workbook.write(out);
			out.close();
			log.info("CalendarDaylightSavingsData.xlsx written successfully on disk.");
			workbook.close();
			
			return getByteArrayFromFile(FILE_PATH);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}
		
		return null;
	}
	
	public Page<CalendarDayLightSavingDto> getAllCalendarDaylightSaving(Pageable pageable) {
        Page<CalendarDayLightSaving> calendarDaylightSavingPage = calendarDayLightSavingRepository.findAll(pageable);
        return calendarDaylightSavingPage.map(this::convertToDto);
    }
	
	private CalendarDayLightSavingDto convertToDto(CalendarDayLightSaving entity) {
		CalendarDayLightSavingDto dto = new CalendarDayLightSavingDto();
        
        dto.setCalendarKey(entity.getCalendar().getCalendarKey());
        dto.setDstYear(entity.getDstYear());
        dto.setSwitchToSummerTime(entity.getSwitchToSummerTime());
        dto.setReturnToStandardTime(entity.getReturnToStandardTime());
        dto.setHourOffset(entity.getHourOffset());
        dto.setRemark(entity.getRemark());
        return dto;
    }

	private void createHeaderRow(XSSFWorkbook workbook, XSSFSheet sheet) {
		Font boldFont = workbook.createFont();
	    boldFont.setBold(true);
	    CellStyle boldStyle = workbook.createCellStyle();
	    boldStyle.setFont(boldFont);
	    
		XSSFRow headerRow = sheet.createRow(0);
		sheet.createFreezePane(0, 1);
		int lastColumnNum = 5;
		sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, lastColumnNum));
		for(int i = 0; i < 6; i++) {
			XSSFCell headerCell = headerRow.createCell(i);
			headerCell.setCellValue(headerColumns[i]);
			headerCell.setCellStyle(boldStyle);
		}
	}
	
	private void populateData(XSSFSheet sheet, XSSFWorkbook workbook, List<CalendarDayLightSaving> listCalendarDayLightSaving) {
		int rowNum = 1;
		
		//Create a cell style to accommodate date format
		CreationHelper helper = workbook.getCreationHelper();
		CellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(helper.createDataFormat().getFormat(DATE_FORMAT));
		
		for(CalendarDayLightSaving calendarDayLightSaving : listCalendarDayLightSaving) {
			XSSFRow dataRow = sheet.createRow(rowNum);
			XSSFCell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(calendarDayLightSaving.getCalendar().getCalendarKey());
			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(calendarDayLightSaving.getDstYear());
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(calendarDayLightSaving.getSwitchToSummerTime());
			dataCell.setCellStyle(dateStyle);
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(calendarDayLightSaving.getReturnToStandardTime());
			dataCell.setCellStyle(dateStyle);
			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(calendarDayLightSaving.getHourOffset());
			dataCell = dataRow.createCell(5);
			dataCell.setCellValue(calendarDayLightSaving.getRemark());

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
