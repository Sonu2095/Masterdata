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

import com.avaya.amsp.domain.CalendarHolidays;
import com.avaya.amsp.masterdata.dtos.CalendarHolidaysDto;
import com.avaya.amsp.masterdata.repo.CalendarHolidaysRepository;

import lombok.extern.slf4j.Slf4j;

@Service("ExportCalendarHolidaysDataService")
@Slf4j
public class ExportCalendarHolidaysDataService {
	
	private static final String FILE_PATH = "/tmp/CalendarHolidaysData.xlsx";
	private static final String SHEET_NAME = "CalendarHolidaysData";
	private static final String DATE_FORMAT = "d/m/yy";
	private static final String EMPTY_STRING = "";
	
	@Autowired
	private CalendarHolidaysRepository calendarHolidaysRepository;
	
	private static final String[] headerColumns = new String[]{"Calendar key", "Holiday name", "Valid each year", "Date", "Remark"};
	
	public byte[] exportCalendarHolidayData() {
		log.info("Request received to export CalendarHolidaysData");
		List<CalendarHolidays> calendarHolidays = calendarHolidaysRepository.findAll();
		
		XSSFWorkbook workbook = new XSSFWorkbook(); // Create Excel workbook
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME); //Create sheet
		
		//Create header row
		createHeaderRow(workbook, sheet);
		//Populate data
		populateData(sheet, workbook, calendarHolidays);
		
		try {
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH));
			workbook.write(out);
			out.close();
			log.info("CalendarHolidaysData.xlsx written successfully on disk.");
			workbook.close();
			
			return getByteArrayFromFile(FILE_PATH);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}
		
		return null;
	}
	
	public Page<CalendarHolidaysDto> getAllCalendarHolidayData(Pageable pageable) {
        Page<CalendarHolidays> calendarHolidaysPage = calendarHolidaysRepository.findAll(pageable);
        return calendarHolidaysPage.map(this::convertToDto);
    }
	
	private CalendarHolidaysDto convertToDto(CalendarHolidays entity) {
        CalendarHolidaysDto dto = new CalendarHolidaysDto();
        
        dto.setCalendarKey(entity.getCalendar().getCalendarKey());
        dto.setHolidayName(entity.getHolidayName());
        dto.setValidEachYear(entity.getValidEachYear());
        dto.setHolidayDate(entity.getHolidayDate());  // Use LocalDate directly
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
		int lastColumnNum = 4;
		sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, lastColumnNum));
		for(int i = 0; i < 5; i++) {
			XSSFCell headerCell = headerRow.createCell(i);
			headerCell.setCellValue(headerColumns[i]);
			headerCell.setCellStyle(boldStyle);
		}
	}
	
	private void populateData(XSSFSheet sheet, XSSFWorkbook workbook, List<CalendarHolidays> calendarHolidays) {
		int rowNum = 1;
		
		//Create a cell style to accommodate date format
		CreationHelper helper = workbook.getCreationHelper();
		CellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(helper.createDataFormat().getFormat(DATE_FORMAT));
		
		for(CalendarHolidays holiday : calendarHolidays) {
			XSSFRow dataRow = sheet.createRow(rowNum);
			XSSFCell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(holiday.getCalendar() != null ? holiday.getCalendar().getCalendarKey() : EMPTY_STRING);
			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(holiday.getHolidayName());
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(holiday.getValidEachYear());
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(holiday.getHolidayDate());
			dataCell.setCellStyle(dateStyle);
			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(holiday.getRemark());
			
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
