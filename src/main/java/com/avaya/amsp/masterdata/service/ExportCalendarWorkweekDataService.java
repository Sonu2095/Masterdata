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

import com.avaya.amsp.domain.CalendarWorkWeek;
import com.avaya.amsp.masterdata.dtos.CalendarWorkWeekDto;
import com.avaya.amsp.masterdata.repo.CalendarWorkWeekRepository;

import lombok.extern.slf4j.Slf4j;

@Service("ExportCalendarWorkweekDataService")
@Slf4j
public class ExportCalendarWorkweekDataService {
	
	private static final String FILE_PATH = "/tmp/CalendarWorkweekData.xlsx";
	private static final String SHEET_NAME = "CalendarWorkweekData";
	private static final String TIME_FORMAT = "hh:mm";
	
	@Autowired
	private CalendarWorkWeekRepository calendarWorkWeekRepository;
	
	private static final String[] headerColumns = new String[]{"Calendar key","Weekday key", "Work time start","Work time end","Remark"};
	
	public byte[] exportCalendarWorkweekData() {
		log.info("Request received to export CalendarWorkweek Data");
		List<CalendarWorkWeek> calendarWorkWeekData = calendarWorkWeekRepository.findAll();
		
		XSSFWorkbook workbook = new XSSFWorkbook(); // Create Excel workbook
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME); //Create sheet
		
		//Create header row
		createHeaderRow(workbook, sheet);
		//Populate data
		populateData(sheet, workbook, calendarWorkWeekData);
		
		try {
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH));
			workbook.write(out);
			out.close();
			log.info("CalendarWorkweekData.xlsx written successfully on disk.");
			workbook.close();
			
			return getByteArrayFromFile(FILE_PATH);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}
		
		return null;
	}
	
	public Page<CalendarWorkWeekDto> getAllCalendarWorkweekData(Pageable pageable) {
        Page<CalendarWorkWeek> calendarWorkweekPage = calendarWorkWeekRepository.findAll(pageable);
        return calendarWorkweekPage.map(this::convertToDto);
    }
	
	private CalendarWorkWeekDto convertToDto(CalendarWorkWeek entity) {
		CalendarWorkWeekDto dto = new CalendarWorkWeekDto();
        
        dto.setCalendarKey(entity.getCalendar().getCalendarKey());
        dto.setWeekdayKey(entity.getWeekdayKey().name());
        dto.setWorkTimeStart(entity.getCalendar().getWorkTimeStart());
        dto.setWorkTimeEnd(entity.getCalendar().getWorkTimeEnd());
        dto.setRemark(entity.getCalendar().getWorkweekRemark());
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
	
	private void populateData(XSSFSheet sheet, XSSFWorkbook workbook, List<CalendarWorkWeek> calendarWorkWeekData) {
		int rowNum = 1;
		
		//Create a cell style to accommodate date format
		CreationHelper helper = workbook.getCreationHelper();
		CellStyle timeStyle = workbook.createCellStyle();
		timeStyle.setDataFormat(helper.createDataFormat().getFormat(TIME_FORMAT));
		
		for(CalendarWorkWeek calendarWorkWeek : calendarWorkWeekData) {
			XSSFRow dataRow = sheet.createRow(rowNum);
			XSSFCell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(calendarWorkWeek.getCalendar().getCalendarKey());
			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(calendarWorkWeek.getWeekdayKey().name());
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(calendarWorkWeek.getCalendar().getWorkTimeStart().toString());
			dataCell.setCellStyle(timeStyle);
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(calendarWorkWeek.getCalendar().getWorkTimeEnd().toString());
			dataCell.setCellStyle(timeStyle);
			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(calendarWorkWeek.getCalendar().getWorkweekRemark());
			
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
