package com.avaya.amsp.masterdata.rest;

import com.avaya.amsp.masterdata.dtos.GetExtensionErrorResponseDto;
import com.avaya.amsp.masterdata.dtos.GetExtensionResponseDto;
import com.avaya.amsp.masterdata.dtos.ListExtensionRequestDto;
import com.avaya.amsp.masterdata.service.ListExtensionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/v1")
public class ListExtensionController {

    @Autowired
    private ListExtensionService listExtensionService;

    @PostMapping("/listExtension")
    // @PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")  // TODO: Uncomment for production - temporarily disabled for testing
    public ResponseEntity<Object> listExtension(@Valid @RequestBody ListExtensionRequestDto request, BindingResult bindingResult) {
        log.info("Request received to list extensions for areaCode: {}, fromExtension: {}, toExtension: {}", 
                request.getAreaCode(), request.getFromExtension(), request.getToExtension());

        // Validate request
        if (bindingResult.hasErrors()) {
            GetExtensionErrorResponseDto errorResponse = new GetExtensionErrorResponseDto();
            errorResponse.setCode(400);
            errorResponse.setType("Bad request");
            
            GetExtensionErrorResponseDto.ErrorMessage errorMessage = new GetExtensionErrorResponseDto.ErrorMessage();
            if (bindingResult.getFieldError("areaCode") != null) {
                errorMessage.setErrorID(-100);
                errorMessage.setErrorText("Area code was not found");
            } else if (bindingResult.getFieldError("fromExtension") != null || bindingResult.getFieldError("toExtension") != null) {
                errorMessage.setErrorID(-121);
                errorMessage.setErrorText("Wrong extension range");
            } else {
                errorMessage.setErrorID(-1);
                errorMessage.setErrorText("Invalid request parameters");
            }
            errorResponse.setMessage(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Validate extensions are digit strings
        if (request.getFromExtension() != null && !request.getFromExtension().matches("\\d+")) {
            GetExtensionErrorResponseDto errorResponse = new GetExtensionErrorResponseDto();
            errorResponse.setCode(400);
            errorResponse.setType("Bad request");
            
            GetExtensionErrorResponseDto.ErrorMessage errorMessage = new GetExtensionErrorResponseDto.ErrorMessage();
            errorMessage.setErrorID(-117);
            errorMessage.setErrorText("Extension is not a digit string");
            errorResponse.setMessage(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        if (request.getToExtension() != null && !request.getToExtension().matches("\\d+")) {
            GetExtensionErrorResponseDto errorResponse = new GetExtensionErrorResponseDto();
            errorResponse.setCode(400);
            errorResponse.setType("Bad request");
            
            GetExtensionErrorResponseDto.ErrorMessage errorMessage = new GetExtensionErrorResponseDto.ErrorMessage();
            errorMessage.setErrorID(-117);
            errorMessage.setErrorText("Extension is not a digit string");
            errorResponse.setMessage(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Validate extension range (toExtension must be >= fromExtension)
        try {
            int fromExt = Integer.parseInt(request.getFromExtension());
            int toExt = Integer.parseInt(request.getToExtension());
            
            if (toExt < fromExt) {
                GetExtensionErrorResponseDto errorResponse = new GetExtensionErrorResponseDto();
                errorResponse.setCode(400);
                errorResponse.setType("Bad request");
                
                GetExtensionErrorResponseDto.ErrorMessage errorMessage = new GetExtensionErrorResponseDto.ErrorMessage();
                errorMessage.setErrorID(-121);
                errorMessage.setErrorText("Wrong extension range");
                errorResponse.setMessage(errorMessage);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        } catch (NumberFormatException e) {
            GetExtensionErrorResponseDto errorResponse = new GetExtensionErrorResponseDto();
            errorResponse.setCode(400);
            errorResponse.setType("Bad request");
            
            GetExtensionErrorResponseDto.ErrorMessage errorMessage = new GetExtensionErrorResponseDto.ErrorMessage();
            errorMessage.setErrorID(-117);
            errorMessage.setErrorText("Extension is not a digit string");
            errorResponse.setMessage(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Validate maxCount if provided
        if (request.getMaxCount() != null && request.getMaxCount() <= 0) {
            GetExtensionErrorResponseDto errorResponse = new GetExtensionErrorResponseDto();
            errorResponse.setCode(400);
            errorResponse.setType("Bad request");
            
            GetExtensionErrorResponseDto.ErrorMessage errorMessage = new GetExtensionErrorResponseDto.ErrorMessage();
            errorMessage.setErrorID(-135);
            errorMessage.setErrorText("Wrong maximum number of delivered extensions");
            errorResponse.setMessage(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            List<GetExtensionResponseDto> response = listExtensionService.listExtensions(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            log.error("Error while listing extensions: {}", ex.getMessage(), ex);
            GetExtensionErrorResponseDto errorResponse = new GetExtensionErrorResponseDto();
            errorResponse.setCode(400);
            errorResponse.setType("Bad request");
            
            GetExtensionErrorResponseDto.ErrorMessage errorMessage = new GetExtensionErrorResponseDto.ErrorMessage();
            errorMessage.setErrorID(-100);
            errorMessage.setErrorText("Area code was not found");
            errorResponse.setMessage(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}

