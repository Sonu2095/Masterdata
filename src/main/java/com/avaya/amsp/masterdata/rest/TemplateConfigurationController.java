package com.avaya.amsp.masterdata.rest;


import com.avaya.amsp.masterdata.dtos.TemplateConfigurationDTO;
import com.avaya.amsp.masterdata.service.iface.TemplateConfigurationServiceIface;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/v1/template-configs")
public class TemplateConfigurationController {

    @Autowired
    TemplateConfigurationServiceIface service;

    @PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
    @GetMapping
    public List< TemplateConfigurationDTO > getAll() {
        return service.getAll();
    }

    @PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
    @PostMapping
    public ResponseEntity<String> create(@Valid  @RequestBody TemplateConfigurationDTO dto) {
        log.info("Request received to persist template configuration {}",dto);
        service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("");
    }

    @PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody TemplateConfigurationDTO dto) {
        service.update(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    @PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}


