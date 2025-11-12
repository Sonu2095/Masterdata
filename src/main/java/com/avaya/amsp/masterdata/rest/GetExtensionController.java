package com.avaya.amsp.masterdata.rest;

import com.avaya.amsp.masterdata.dtos.GetExtensionErrorResponseDto;
import com.avaya.amsp.masterdata.dtos.GetExtensionRequestDto;
import com.avaya.amsp.masterdata.dtos.GetExtensionResponseDto;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.service.GetExtensionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/v1")
public class GetExtensionController {

    @Autowired
    private GetExtensionService getExtensionService;

    @PostMapping("/getExtension")
    @PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
    public ResponseEntity<Object> getExtension(@Valid @RequestBody GetExtensionRequestDto request, BindingResult bindingResult) {
        log.info("Request received to get extension for areaCode: {} and extension: {}", request.getAreaCode(), request.getExtension());

        // Validate request
        if (bindingResult.hasErrors()) {
            GetExtensionErrorResponseDto errorResponse = new GetExtensionErrorResponseDto();
            errorResponse.setCode(400);
            errorResponse.setType("Bad request");
            
            GetExtensionErrorResponseDto.ErrorMessage errorMessage = new GetExtensionErrorResponseDto.ErrorMessage();
            // Check for specific validation errors
            if (bindingResult.getFieldError("areaCode") != null) {
                errorMessage.setErrorID(-100);
                errorMessage.setErrorText("Area code was not found");
            } else if (bindingResult.getFieldError("extension") != null) {
                errorMessage.setErrorID(-101);
                errorMessage.setErrorText("Extension was not found for the given area code");
            } else {
                errorMessage.setErrorID(-1);
                errorMessage.setErrorText("Invalid request parameters");
            }
            errorResponse.setMessage(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Validate extension is a digit string
        if (request.getExtension() != null && !request.getExtension().matches("\\d+")) {
            GetExtensionErrorResponseDto errorResponse = new GetExtensionErrorResponseDto();
            errorResponse.setCode(400);
            errorResponse.setType("Bad request");
            
            GetExtensionErrorResponseDto.ErrorMessage errorMessage = new GetExtensionErrorResponseDto.ErrorMessage();
            errorMessage.setErrorID(-117);
            errorMessage.setErrorText("Extension is not a digit string");
            errorResponse.setMessage(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            GetExtensionResponseDto response = getExtensionService.getExtensionByAreaCodeAndExtension(
                    request.getAreaCode(), 
                    request.getExtension()
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ResourceNotFoundException ex) {
            log.warn("Subscriber not found: {}", ex.getMessage());
            GetExtensionErrorResponseDto errorResponse = new GetExtensionErrorResponseDto();
            errorResponse.setCode(400);
            errorResponse.setType("Bad request");
            
            GetExtensionErrorResponseDto.ErrorMessage errorMessage = new GetExtensionErrorResponseDto.ErrorMessage();
            errorMessage.setErrorID(-101);
            errorMessage.setErrorText("Extension was not found for the given area code");
            errorResponse.setMessage(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception ex) {
            log.error("Error while fetching extension: {}", ex.getMessage(), ex);
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

