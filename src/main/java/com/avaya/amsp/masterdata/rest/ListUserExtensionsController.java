package com.avaya.amsp.masterdata.rest;

import com.avaya.amsp.masterdata.dtos.GetExtensionErrorResponseDto;
import com.avaya.amsp.masterdata.dtos.GetExtensionResponseDto;
import com.avaya.amsp.masterdata.dtos.ListUserExtensionsRequestDto;
import com.avaya.amsp.masterdata.service.ListUserExtensionsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/v1")
public class ListUserExtensionsController {

    @Autowired
    private ListUserExtensionsService listUserExtensionsService;

    @PostMapping("/listUserExtensions")
    public ResponseEntity<Object> listUserExtensions(@Valid @RequestBody ListUserExtensionsRequestDto request, BindingResult bindingResult) {
        log.info("Request received to list user extensions for domain: {}, userID: {}", request.getDomain(), request.getUserID());

        if (bindingResult.hasErrors()) {
            GetExtensionErrorResponseDto errorResponse = new GetExtensionErrorResponseDto();
            errorResponse.setCode(400);
            errorResponse.setType("Bad request");

            GetExtensionErrorResponseDto.ErrorMessage errorMessage = new GetExtensionErrorResponseDto.ErrorMessage();
            if (bindingResult.getFieldError("domain") != null) {
                errorMessage.setErrorID(-126);
                errorMessage.setErrorText("Undefined domain");
            } else if (bindingResult.getFieldError("userID") != null) {
                errorMessage.setErrorID(-127);
                errorMessage.setErrorText("Undefined user ID");
            } else {
                errorMessage.setErrorID(-1);
                errorMessage.setErrorText("Invalid request parameters");
            }
            errorResponse.setMessage(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            List<GetExtensionResponseDto> response = listUserExtensionsService.listUserExtensions(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            log.error("Error while listing user extensions: {}", ex.getMessage(), ex);
            GetExtensionErrorResponseDto errorResponse = new GetExtensionErrorResponseDto();
            errorResponse.setCode(400);
            errorResponse.setType("Bad request");

            GetExtensionErrorResponseDto.ErrorMessage errorMessage = new GetExtensionErrorResponseDto.ErrorMessage();
            errorMessage.setErrorID(-1);
            errorMessage.setErrorText("Invalid request");
            errorResponse.setMessage(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}

