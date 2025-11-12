package com.avaya.amsp.masterdata.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.CellStyle;
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

import com.avaya.amsp.domain.Calendar;
import com.avaya.amsp.domain.Subscribers;
import com.avaya.amsp.masterdata.dtos.CalendarDto;
import com.avaya.amsp.masterdata.dtos.SubscriberDto;
import com.avaya.amsp.masterdata.repo.CalendarRepository;

import lombok.extern.slf4j.Slf4j;

@Service("ExportCalendarDataService")
@Slf4j
public class ExportCalendarDataService {
	
	private static final String FILE_PATH = "/tmp/CalendarData.xlsx";
	private static final String SHEET_NAME = "CalendarData";
	
	@Autowired
	private CalendarRepository calendarRepo;
	
	private static final String[] headerColumns = new String[]{"Calendar key", "Started from Year", "Available until Year", "Clock change base", "Remark"};
	
	public byte[] exportCalendarData() {
		log.info("Request received to export CalendarData");
		List<Calendar> calendarItems = calendarRepo.findAll();
		
		XSSFWorkbook workbook = new XSSFWorkbook(); // Create Excel workbook
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME); //Create sheet
		
		//Create header row
		createHeaderRow(workbook, sheet);
		//Populate data
		populateData(sheet, workbook, calendarItems);
		
		try {
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH));
			workbook.write(out);
			out.close();
			log.info("CalendarData.xlsx written successfully on disk.");
			workbook.close();
			
			return getByteArrayFromFile(FILE_PATH);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}
		
		return null;
	}
	
    public Page<CalendarDto> getAllCalendars(Pageable pageable) {
    	log.info("Request received to get CalendarData");
        Page<Calendar> calendarsPage = calendarRepo.findAll(pageable);
        return calendarsPage.map(this::convertToDto);
    }
    
    
    private CalendarDto convertToDto(Calendar calendar) {
    	CalendarDto dto = new CalendarDto();
        dto.setCalendarKey(calendar.getCalendarKey());
        dto.setYearFrom(calendar.getYearFrom());
        dto.setYearTo(calendar.getYearTo());
        dto.setDescription(calendar.getDescription());
        dto.setClockChangeBase(calendar.getClockChangeBase() != null ? calendar.getClockChangeBase().name() : null); // Map Enum to String
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
	
	private void populateData(XSSFSheet sheet, XSSFWorkbook workbook, List<Calendar> calendarItems) {
		int rowNum = 1;
		
		for(Calendar calendarItem : calendarItems) {
			XSSFRow dataRow = sheet.createRow(rowNum);
			XSSFCell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(calendarItem.getCalendarKey());
			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(calendarItem.getYearFrom());
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(calendarItem.getYearTo());
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(calendarItem.getClockChangeBase().name());
			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(calendarItem.getDescription());
			
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
