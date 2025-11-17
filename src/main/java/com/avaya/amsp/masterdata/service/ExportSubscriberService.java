package com.avaya.amsp.masterdata.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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

import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.Subscribers;
import com.avaya.amsp.masterdata.dtos.SubscriberDto;
import com.avaya.amsp.masterdata.repo.ClusterRepository;
import com.avaya.amsp.masterdata.repo.PbxSystemRepository;
import com.avaya.amsp.masterdata.repo.RegionRepository;
import com.avaya.amsp.masterdata.repo.SiteRepository;
import com.avaya.amsp.masterdata.repo.SubscriberRepository;

import lombok.extern.slf4j.Slf4j;

@Service("ExportSubscriberService")
@Slf4j
public class ExportSubscriberService {

	private static final String FILE_PATH = "/tmp/SubscriberData.xlsx";
	private static final String SHEET_NAME = "SubscriberData";
	private static final String DATE_FORMAT = "d/m/yy h:mm";

	@Autowired
	private SubscriberRepository subscriberRepo;
	
	@Autowired
	private ClusterRepository clusterRepo;
	
	@Autowired
	private PbxSystemRepository pbxSystemRepository;
	
	@Autowired
	private SiteRepository siteRepo;
	
	@Autowired
	private RegionRepository  regionRepo;

	private static final String[] headerColumns = new String[] { "Cluster",
			"Area Code", "Site", "Extension", "E164", "Name", "First Name", "NT Username", 
			"NT Domain", "Cost center", "Email", "Fax", "Mobile", "Pager",
			"Department", "Cicat status", "Account type", "Room office", "Type of use",
			"Remark", "Connection type", " BCS bunch", "Msn master", "Automatic sync", 
			"Data record blocked", "Amfk expansion", "From user", "When pin available",
			"Current state", "Compas status", "Last verification at", "Last seen registered", "Cicat office", "User stamp", "Timestamp(SY)"};

	public byte[] exportSubscriberData(Long clusterKey) {
		log.info("Request received to export subscriber data for cluster {}", clusterKey);
		List<Subscribers> subscribers = subscriberRepo.findByIdCluster(clusterKey, null).getContent();

		XSSFWorkbook workbook = new XSSFWorkbook(); // Create Excel workbook
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME); // Create sheet

		// Create header row 
		createHeaderRow(workbook, sheet); //Populate data
		populateData(sheet, workbook, subscribers);

		try {
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH));
			workbook.write(out);
			out.close();
			log.info("SubscriberData.xlsx written successfully on disk.");
			workbook.close();

			return getByteArrayFromFile(FILE_PATH);
		} catch (Exception e) {
			log.error("Error while creating excel sheet");
		}

		return null;
	}
	
	public Page<SubscriberDto> getSubscriberData(Long clusterKey, Pageable pageable) {
		log.info("Request received to get subscriber data for cluster {}", clusterKey);
		Page<Subscribers> subscribersPage = subscriberRepo.findByIdCluster(clusterKey, pageable);
		return subscribersPage.map(this::convertToDto);
	}

	private SubscriberDto convertToDto(Subscribers entity) {
		SubscriberDto dto = new SubscriberDto();
        
		dto.setId(entity.getId());
		dto.setIdCluster(entity.getIdCluster());
		dto.setIdPbx(String.valueOf(entity.getIdPbxSystem()));
		dto.setIdRegion(entity.getIdRegion());
		dto.setIdSite(entity.getIdSite());
        dto.setAccountType(entity.getAccountType());
        dto.setAmfkExpansion(entity.getAmfkExpansion());
        dto.setAreaCode(entity.getAreaCode());
        dto.setAutomaticDmtSync(entity.getAutomaticSync() != null ? entity.getAutomaticSync().name() : "");
        dto.setBcsBunch(entity.getBcsBunch());
        dto.setCicatStatus(entity.getCicatStatus());
        dto.setClusterName(getClusterNameById(entity.getIdCluster()));
        dto.setCompasStatus(entity.getCompasStatus());
        dto.setConnectionType(entity.getConnectionType());
        dto.setCostCenter(entity.getCostCenter());
        dto.setCurrentState(entity.getCurrentState());
        dto.setDataRecordBlocked(entity.getDataRecordBlocked());
        dto.setDepartment(entity.getDepartment());
        dto.setE164(entity.getE164());
        dto.setEmail(entity.getEmail());
        dto.setExtension(entity.getExtension());
        dto.setFax(entity.getFax());
        dto.setFirstName(entity.getFirstName());
        dto.setFromUser(entity.getFromUser());
        dto.setLastSeenRegistered(entity.getLastSeenRegistered());
        dto.setLastVerificationAt(entity.getLastVerificationAt());
        dto.setMobile(entity.getMobile());
        dto.setMsnMaster(entity.getMsnMaster());
        dto.setName(entity.getName());
        dto.setNtDomain(entity.getNtDomain());
        dto.setNtUsername(entity.getNtUsername());
        dto.setPager(entity.getPager());
        dto.setPbxName(getPBXSystemNameById(entity.getIdPbxSystem()));
        dto.setRegionName(getRegionNameById(entity.getIdRegion()));
        dto.setRemark(entity.getRemark());
        dto.setRoomOffice(entity.getRoomOffice());
        dto.setSiteName(getSiteNameById(entity.getIdSite()));
        dto.setTypeOfUse(entity.getTypeOfUse());
        dto.setWhenPinAvailable(entity.getWhenPinAvailable());
        dto.setUserStamp(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : entity.getCreatedBy());
        LocalDateTime time = entity.getUpdatedAt() != null ? entity.getUpdatedAt() : entity.getCreatedAt();
        dto.setTimeStamp(time != null ? Timestamp.valueOf(time) : null);
        dto.setCicatOffice(entity.getCicatOffice());
        
        return dto;
    }
	
	private String getPBXSystemNameById(Long pbxSystemId) {
        if (pbxSystemId != null) {
            String name = pbxSystemRepository.findNameById(pbxSystemId);  // Assuming the repository method exists
            if (name == null) {
                log.warn("PBX System not found for ID {}", pbxSystemId);
                return String.valueOf(pbxSystemId);  // Return the ID if no name is found
            }
            return name;
        }
        return String.valueOf(pbxSystemId);  // Return the ID if clusterId is null
    }
	
	private String getClusterNameById(Long clusterId) {
        if (clusterId != null) {
            String name = clusterRepo.findNameById(clusterId);  // Assuming the repository method exists
            if (name == null) {
                log.warn("Cluster not found for ID {}", clusterId);
                return String.valueOf(clusterId);  // Return the ID if no name is found
            }
            return name;
        }
        return String.valueOf(clusterId);  // Return the ID if clusterId is null
    }

    private String getSiteNameById(Long siteId) {
        if (siteId != null) {
            String name = siteRepo.findNameById(siteId);  // Assuming the repository method exists
            if (name == null) {
                log.warn("Site not found for ID {}", siteId);
                return String.valueOf(siteId);  // Return the ID if no name is found
            }
            return name;
        }
        return String.valueOf(siteId);  // Return the ID if siteId is null
    }

    private String getRegionNameById(Long regionId) {
        if (regionId != null) {
            String name = regionRepo.findNameById(regionId);  // Assuming the repository method exists
            if (name == null) {
                log.warn("Region not found for Site ID {}", regionId);
                return String.valueOf(regionId);  // Return the ID if no name is found
            }
            return name;
        }
        return String.valueOf(regionId);  // Return the ID if siteId is null
    }
	
	private void createHeaderRow(XSSFWorkbook workbook, XSSFSheet sheet) {
		Font boldFont = workbook.createFont();
		boldFont.setBold(true);
		CellStyle boldStyle = workbook.createCellStyle();
		boldStyle.setFont(boldFont);

		XSSFRow headerRow = sheet.createRow(0);
		sheet.createFreezePane(0, 1);
		int lastColumnNum = 34;
		sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, lastColumnNum));
		for (int i = 0; i < 35; i++) {
			XSSFCell headerCell = headerRow.createCell(i);
			headerCell.setCellValue(headerColumns[i]);
			headerCell.setCellStyle(boldStyle);
		}
	}

	private void populateData(XSSFSheet sheet, XSSFWorkbook workbook, List<Subscribers> subscribers) {
		int rowNum = 1;

		// Create a cell style to accommodate date format 
		CreationHelper helper = workbook.getCreationHelper();
		CellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(helper.createDataFormat().getFormat(DATE_FORMAT));
		
		ClusterItem clusterItem = clusterRepo.findById(subscribers.get(0).getIdCluster()).get();
		
		for (Subscribers subscriber : subscribers) {
			XSSFRow dataRow = sheet.createRow(rowNum);
			XSSFCell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(clusterItem.getName());
			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(subscriber.getAreaCode());
			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(getSiteNameById(subscriber.getIdSite()));
			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(subscriber.getExtension());
			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(subscriber.getE164());
			dataCell = dataRow.createCell(5);
			dataCell.setCellValue(subscriber.getName());
			dataCell = dataRow.createCell(6);
			dataCell.setCellValue(subscriber.getFirstName());
			dataCell = dataRow.createCell(7);
			dataCell.setCellValue(subscriber.getNtUsername());
			dataCell = dataRow.createCell(8);
			dataCell.setCellValue(subscriber.getNtDomain());
			dataCell = dataRow.createCell(9);
			dataCell.setCellValue(subscriber.getCostCenter());
			dataCell = dataRow.createCell(10);
			dataCell.setCellValue(subscriber.getEmail()); 
			dataCell = dataRow.createCell(11);
			dataCell.setCellValue(subscriber.getFax());
			dataCell = dataRow.createCell(12);
			dataCell.setCellValue(subscriber.getMobile());
			dataCell = dataRow.createCell(13);
			dataCell.setCellValue(subscriber.getPager());
			dataCell = dataRow.createCell(14);
			dataCell.setCellValue(subscriber.getDepartment());
			dataCell = dataRow.createCell(15);
			dataCell.setCellValue(subscriber.getCicatStatus());
			dataCell = dataRow.createCell(16);
			dataCell.setCellValue(subscriber.getAccountType());
			dataCell = dataRow.createCell(17);
			dataCell.setCellValue(subscriber.getRoomOffice());
			dataCell = dataRow.createCell(18);
			dataCell.setCellValue(subscriber.getTypeOfUse());
			dataCell = dataRow.createCell(19);
			dataCell.setCellValue(subscriber.getRemark());
			dataCell = dataRow.createCell(20);
			dataCell.setCellValue(subscriber.getConnectionType());
			dataCell = dataRow.createCell(21);
			dataCell.setCellValue(subscriber.getBcsBunch());
			dataCell = dataRow.createCell(22);
			dataCell.setCellValue(subscriber.getMsnMaster());
			dataCell = dataRow.createCell(23);
			dataCell.setCellValue(subscriber.getAutomaticSync() != null ? subscriber.getAutomaticSync().name() : "");
			dataCell = dataRow.createCell(24);
			dataCell.setCellValue(subscriber.getDataRecordBlocked() != null ? subscriber.getDataRecordBlocked().booleanValue() : false);
			dataCell = dataRow.createCell(25);
			dataCell.setCellValue(subscriber.getAmfkExpansion() != null ? subscriber.getAmfkExpansion().booleanValue() : false);
			dataCell = dataRow.createCell(26);
			dataCell.setCellValue(subscriber.getFromUser());
			dataCell = dataRow.createCell(27);
			dataCell.setCellValue(subscriber.getWhenPinAvailable() != null ? subscriber.getWhenPinAvailable().booleanValue() : false);
			dataCell = dataRow.createCell(28);
			dataCell.setCellValue(subscriber.getCurrentState());
			dataCell = dataRow.createCell(29);
			dataCell.setCellValue(subscriber.getCompasStatus());
			dataCell = dataRow.createCell(30);
			dataCell.setCellValue(subscriber.getLastVerificationAt());
			dataCell.setCellStyle(dateStyle);
			dataCell = dataRow.createCell(31);
			dataCell.setCellValue(subscriber.getLastSeenRegistered());
			dataCell.setCellStyle(dateStyle);
			dataCell = dataRow.createCell(32);
			dataCell.setCellValue(subscriber.getCicatOffice());
			dataCell = dataRow.createCell(33);
			dataCell.setCellValue((subscriber.getUpdatedBy() != null && !subscriber.getUpdatedBy().isEmpty()) ? subscriber.getUpdatedBy() : subscriber.getCreatedBy());
			dataCell = dataRow.createCell(34);
			dataCell.setCellValue((subscriber.getUpdatedAt() != null) ? subscriber.getUpdatedAt() : subscriber.getCreatedAt());
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
