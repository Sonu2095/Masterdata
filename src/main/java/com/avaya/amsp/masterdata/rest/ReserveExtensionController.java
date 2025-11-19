package com.avaya.amsp.masterdata.rest;

import com.avaya.amsp.masterdata.dtos.GetExtensionErrorResponseDto;
import com.avaya.amsp.masterdata.dtos.GetExtensionResponseDto;
import com.avaya.amsp.masterdata.dtos.ReserveExtensionRequestDto;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.service.ReserveExtensionService;
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
public class ReserveExtensionController {

    @Autowired
    private ReserveExtensionService reserveExtensionService;

    @PostMapping("/reserveExtension")
    // @PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")  // TODO: Uncomment for production - temporarily disabled for testing
    public ResponseEntity<Object> reserveExtension(@Valid @RequestBody ReserveExtensionRequestDto request, BindingResult bindingResult) {
        log.info("Request received to reserve extension for areaCode: {} and extension: {}", 
                request.getCallNumber() != null ? request.getCallNumber().getAreacode() : null,
                request.getCallNumber() != null ? request.getCallNumber().getExtension() : null);

        // Validate request
        if (bindingResult.hasErrors()) {
            GetExtensionErrorResponseDto errorResponse = new GetExtensionErrorResponseDto();
            errorResponse.setCode(400);
            errorResponse.setType("Bad request");
            
            GetExtensionErrorResponseDto.ErrorMessage errorMessage = new GetExtensionErrorResponseDto.ErrorMessage();
            if (bindingResult.getFieldError("callNumber") != null) {
                errorMessage.setErrorID(-100);
                errorMessage.setErrorText("Area code was not found");
            } else if (bindingResult.hasFieldErrors("callNumber.areacode")) {
                errorMessage.setErrorID(-100);
                errorMessage.setErrorText("Area code was not found");
            } else if (bindingResult.hasFieldErrors("callNumber.extension")) {
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
        if (request.getCallNumber() != null && request.getCallNumber().getExtension() != null 
                && !request.getCallNumber().getExtension().matches("\\d+")) {
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
            GetExtensionResponseDto response = reserveExtensionService.reserveExtension(
                    request.getCallNumber().getAreacode(),
                    request.getCallNumber().getExtension(),
                    request.getMaxReservationTimeInSecs()
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
        } catch (IllegalStateException ex) {
            log.warn("Extension reservation failed: {}", ex.getMessage());
            GetExtensionErrorResponseDto errorResponse = new GetExtensionErrorResponseDto();
            errorResponse.setCode(400);
            errorResponse.setType("Bad request");
            
            GetExtensionErrorResponseDto.ErrorMessage errorMessage = new GetExtensionErrorResponseDto.ErrorMessage();
            if ("EXTENSION_ALREADY_RESERVED".equals(ex.getMessage())) {
                errorMessage.setErrorID(-132);
                errorMessage.setErrorText("Extension is already reserved");
            } else if ("EXTENSION_NOT_FREE".equals(ex.getMessage())) {
                errorMessage.setErrorID(-136);
                errorMessage.setErrorText("Trying to reserve a non-free extension");
            } else {
                errorMessage.setErrorID(-136);
                errorMessage.setErrorText("Trying to reserve a non-free extension");
            }
            errorResponse.setMessage(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception ex) {
            log.error("Error while reserving extension: {}", ex.getMessage(), ex);
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

