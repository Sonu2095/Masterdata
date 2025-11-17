package com.avaya.amsp.masterdata.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

import com.avaya.amsp.domain.UserDetails;
import com.avaya.amsp.domain.UserExtraInfo;
import com.avaya.amsp.domain.UserPassword;
import com.avaya.amsp.masterdata.dtos.UserDto;
import com.avaya.amsp.masterdata.repo.UserDetailsRepo;

import lombok.extern.slf4j.Slf4j;

@Service("ExportUserDataService")
@Slf4j
public class ExportUserDataService {
	
	private static final String FILE_PATH = "/tmp/UserData.xlsx";
	private static final String SHEET_NAME = "UserData";
	
	@Autowired
	private UserDetailsRepo userDetailsRepo;
	
	private static final String[] headerColumns = new String[]{"User account", "First Name", "Last Name", "Email", "Phone num", "Fax", "Role",
			"Cluster", "Language", "Remark", "Password validity"};
	
	public byte[] exportUserData() {
		log.info("Request received to export User Data");
		
		List<UserDetails> userDetails = userDetailsRepo.findAll();
		List<UserDto> listUserDto = new ArrayList<>();
		UserExtraInfo extraInfo;
		UserPassword userPwd;
		
		for(UserDetails user : userDetails) {
			extraInfo = user.getUserExtraInfo();
			userPwd = user.getUserPassword();
			
			UserDto userDto = new UserDto();
			userDto.setUserAccount(user.getUsername());
			userDto.setFirstName(user.getFname());
			userDto.setLastName(user.getLname());
			userDto.setEmail(user.getEmail());
			userDto.setFax(user.getFaxNum());
			userDto.setPhoneNum(user.getPhoneNum());
			userDto.setLanguage(user.getLanguage().getName());
			userDto.setRemark(user.getRemark());
			userDto.setCluster(extraInfo.getClusterItem() != null ? extraInfo.getClusterItem().getName() : "");
			userDto.setRole(user.getRoles());
			userDto.setPasswordValidity(userPwd.getExpiryAfterDays() != null ? userPwd.getExpiryAfterDays() : 0);
			
			listUserDto.add(userDto);
		}
		
		XSSFWorkbook workbook = new XSSFWorkbook(); // Create Excel workbook
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME); // Create sheet

		// Create header row 
		createHeaderRow(workbook, sheet); 
		//Populate data
		populateData(sheet, workbook, listUserDto);

		try {
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH));
			workbook.write(out);
			out.close();
			log.info("UserData.xlsx written successfully on disk.");
			workbook.close();

			return getByteArrayFromFile(FILE_PATH);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}
		
		return null;
	}
	
	public Page<UserDto> getAllUsers(Pageable pageable) {
		log.info("Request received to get User Data");
        Page<UserDetails> users = userDetailsRepo.findAll(pageable);
        return users.map(this::convertToDto);
    }
    
    private UserDto convertToDto(UserDetails entity) {
    	UserDto dto = new UserDto();
		
		UserExtraInfo extraInfo = entity.getUserExtraInfo();
		UserPassword userPwd = entity.getUserPassword();
		
    	dto.setUserAccount(entity.getUsername());
		dto.setFirstName(entity.getFname());
		dto.setLastName(entity.getLname());
		dto.setEmail(entity.getEmail());
		dto.setFax(entity.getFaxNum());
		dto.setPhoneNum(entity.getPhoneNum());
		dto.setLanguage(entity.getLanguage().getName());
		dto.setRemark(entity.getRemark());
		dto.setCluster(extraInfo.getClusterItem() != null ? extraInfo.getClusterItem().getName() : "");
		dto.setRole(entity.getRoles());
		dto.setPasswordValidity(userPwd.getExpiryAfterDays() != null ? userPwd.getExpiryAfterDays() : 0);
    	
        return dto;
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
		for(int i = 0; i < 11; i++) {
			XSSFCell headerCell = headerRow.createCell(i);
			headerCell.setCellValue(headerColumns[i]);
			headerCell.setCellStyle(boldStyle);
		}
	}
	
	private void populateData(XSSFSheet sheet, XSSFWorkbook workbook, List<UserDto> listUserDto) {
		int rowNum = 1;
		
		for(UserDto user : listUserDto) {
			XSSFRow dataRow = sheet.createRow(rowNum);
			XSSFCell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(user.getUserAccount());
			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(user.getFirstName());
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(user.getLastName());
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(user.getEmail());
			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(user.getPhoneNum());
			dataCell = dataRow.createCell(5);
			dataCell.setCellValue(user.getFax());
			dataCell = dataRow.createCell(6);
			dataCell.setCellValue(user.getRole());
			dataCell = dataRow.createCell(7);
			dataCell.setCellValue(user.getCluster());
			dataCell = dataRow.createCell(8);
			dataCell.setCellValue(user.getLanguage());
			dataCell = dataRow.createCell(9);
			dataCell.setCellValue(user.getRemark());
			dataCell = dataRow.createCell(10);
			dataCell.setCellValue(user.getPasswordValidity());
			
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
