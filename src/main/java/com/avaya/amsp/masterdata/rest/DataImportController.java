package com.avaya.amsp.masterdata.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.avaya.amsp.masterdata.service.ExcelImportService;

@RestController
@RequestMapping("/v1/excelImport")
public class DataImportController {

    @Autowired
    private ExcelImportService excelService;

    @Operation(summary = "Upload an Excel file and insert data into the database")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('TK_SV','AVAYA_ADMIN')")
    public ResponseEntity<String> uploadFile(
        @Parameter(description = "The Excel file to be uploaded", required = true) 
        @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload a file.");
        }

        excelService.saveDataFromExcel(file);

        return ResponseEntity.ok("File uploaded and data inserted successfully.");
    }
}
