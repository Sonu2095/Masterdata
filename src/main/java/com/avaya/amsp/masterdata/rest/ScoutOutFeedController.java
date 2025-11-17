package com.avaya.amsp.masterdata.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.GenericAPIResponseDto;
import com.avaya.amsp.masterdata.service.ScoutOutFeedService;
import com.avaya.amsp.security.user.AMSPUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * REST controller that exposes endpoints for listing and downloading user-authorized cost report files.
 */
@RestController
@RequestMapping("v1")
public class ScoutOutFeedController {
	
	@Autowired
	private ScoutOutFeedService reportGenerator;
	private static final String MEDIA_TYPE_EXCEL = "application/vnd.ms-excel";

	@GetMapping("/cost-reports")
	@Operation(summary = "Returns a list of available cost report file names that the current user is authorized to access.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Reports fetched successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid username or bad request"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@PreAuthorize("hasAnyRole('TK_SV','CC_RESP')")
	public ResponseEntity<GenericAPIResponseDto<List<String>>> getCostReports(@AuthenticationPrincipal AMSPUser amspUser) throws Exception {
		List<String> filePaths = reportGenerator.getCostReportsForUser(amspUser.getUsername());
		
		String message = filePaths.isEmpty() ? "No cost reports available for your assigned cost centers."
						: "Reports fetched successfully.";
		GenericAPIResponseDto<List<String>> response = new GenericAPIResponseDto<>(
				true, 
				message, 
				filePaths, 
				HttpStatus.OK.value()
				);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/cost-reports/{filename}/download")
	@Operation(summary = "Returns the actual file content (as attachment) for the given report file, if the user has access to it.",
			description = "Allows downloading a generated cost report file by specifying the file name. " +
			"The file must exist in the configured report output directory. File name to be entered with extension")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "File downloaded successfully",
					content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
			@ApiResponse(responseCode = "404", description = "File not found"),
			@ApiResponse(responseCode = "400", description = "Invalid file name supplied"),
			@ApiResponse(responseCode = "500", description = "Internal server error during file processing")
	})
	@PreAuthorize("hasAnyRole('TK_SV','CC_RESP')")
	public ResponseEntity<?> downloadCostReport(@PathVariable("filename") String fileName,
			@AuthenticationPrincipal AMSPUser amspUser) throws Exception {

	    Resource fileResource = reportGenerator.getReportFileResource(fileName, amspUser.getUsername());

	    return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL))
		.header("Content-Disposition", "attachment; filename=" + fileName)
		.body(fileResource);
	}

}