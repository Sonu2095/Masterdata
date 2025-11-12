package com.avaya.amsp.masterdata.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.RoleAssignmentRepository;

@Service
public class ScoutOutFeedService {

	@Value("${scoutoutfeed.output.folder}")
	private String reportOutputPath;

	@Autowired
	private RoleAssignmentRepository roleAssignmentRepo;

	private static final Logger logger = LoggerFactory.getLogger(ScoutOutFeedService.class);

	/**
	 * Retrieves the list of cost report files (.xlsx) that a specific user
	 * is authorized to view based on their assigned cost centers.
	 *
	 * @param user the username requesting access to reports
	 * @return list of report file names the user can view
	 * @throws Exception if there is an issue accessing the file system or repository
	 */
	public List<String> getCostReportsForUser(String user) throws Exception {
		logger.info("Fetching cost reports for the user: {}", user);
		List<String> generatedFiles = new ArrayList<>();

	    // Get cost centers assigned to the user
	    List<String> userCostCenters = roleAssignmentRepo.findCostCentersForUser(user);
	    if (userCostCenters == null || userCostCenters.isEmpty()) {
	        logger.warn("No cost centers found for user: {}", user);
	        return generatedFiles;
	    }
	    
	    logger.info("User {} can view cost centers: {}", user, userCostCenters);

		File outputDir = new File(reportOutputPath);
	    if (!outputDir.exists() || !outputDir.isDirectory()) {
	        logger.error("Report output directory does not exist or is invalid: {}", reportOutputPath);
	        return generatedFiles;
	    }
	    
		File[] files = outputDir.listFiles((dir, name) -> name.endsWith(".xlsx"));
	    if (files == null || files.length == 0) {
	        logger.info("No .xlsx files found in {}", reportOutputPath);
	        return generatedFiles;
	    }

		for (File file : files) {
			String fileName = file.getName(); // sample file name - 2025_05_25D611.xlsx
			String[] parts = fileName.split("_");
	        if (parts.length < 3) {
	            logger.debug("Skipping file with unexpected name format: {}", fileName);
	            continue;
	        }
			String costCenterWithExt = parts[2]; 
			String costCenter = costCenterWithExt.substring(0, costCenterWithExt.lastIndexOf('.'));
			if (userCostCenters.contains(costCenter)) {
				generatedFiles.add(fileName); 
			}
		}
		
		logger.info("Found {} cost report(s) for user {}", generatedFiles.size(), user);
		return generatedFiles;
	}

	/**
	 * Returns a file resource for a given report file if the user has access to it.
	 * Performs validation for file name, existence, and user authorization.
	 *
	 * @param fileName name of the requested report file
	 * @param user     username requesting the report
	 * @return {@link org.springframework.core.io.Resource} representing the requested file
	 * @throws ResourceNotFoundException if the file does not exist or is invalid
	 * @throws AccessDeniedException if the user is not authorized to access the file
	 */
	public Resource getReportFileResource(String fileName, String user) throws Exception {
		if (fileName == null || fileName.contains("..") || fileName.contains(File.separator)) {
			logger.warn("Invalid file name requested for cost report download: {}", fileName);
			throw new ResourceNotFoundException("Invalid file name.");
		}

		File file = new File(reportOutputPath, fileName);
		if (!file.exists() || !file.isFile()) {
			logger.warn("Requested report file does not exist: {}", file.getAbsolutePath());
			throw new ResourceNotFoundException("File not found.");
		}

		// Check user access before returning the file
		List<String> userCostReports = getCostReportsForUser(user);
		if(!userCostReports.contains(fileName)) {
			logger.warn("User does not have access to requested report");
			throw new AccessDeniedException("User does not have permission to access this file.");
		}

		logger.info("Returning cost report file for download: {}", file.getAbsolutePath());
		return new FileSystemResource(file);
	}

}
