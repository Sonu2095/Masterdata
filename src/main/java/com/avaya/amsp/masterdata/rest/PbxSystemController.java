package com.avaya.amsp.masterdata.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.domain.PbxSystem;
import com.avaya.amsp.masterdata.dtos.AemPbxDto;
import com.avaya.amsp.masterdata.dtos.PartListDto;
import com.avaya.amsp.masterdata.dtos.PbxClusterDto;
import com.avaya.amsp.masterdata.dtos.PbxComponentDto;
import com.avaya.amsp.masterdata.dtos.PbxPortDto;
import com.avaya.amsp.masterdata.dtos.PbxSpecialPortDto;
import com.avaya.amsp.masterdata.dtos.PbxSystemDto;
import com.avaya.amsp.masterdata.dtos.PbxSystemSiteDto;
import com.avaya.amsp.masterdata.service.PbxSystemService;
import com.avaya.amsp.masterdata.service.iface.PartListServiceIface;
import com.avaya.amsp.masterdata.service.iface.PbxClusterServiceIface;
import com.avaya.amsp.masterdata.service.iface.PbxSystemServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/pbxSystems")
public class PbxSystemController {
	@Autowired
	private PbxSystemService pbxSystemService;

	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchPbxSystems() {
		log.info("requesting for getting all pbxSystems");
		List<PbxSystemDto> response = new ArrayList<>();
		response = pbxSystemService.fetchAllPbxSystems();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@GetMapping("/physicalpbx/{name}/aempbx")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchAemPbxByPhysicalPbx(@PathVariable("name") String physicalPbx) {
		log.info("requesting for getting all pbxSystems");
		AemPbxDto response = pbxSystemService.fetchAemPbxByPhysicalPbx(physicalPbx);
        if (!response.getAemPbx().isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            log.warn("PbxSystem not found for physicalPbx: {}", physicalPbx);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PbxSystem not found for physicalPbx: " + physicalPbx);
        }
	}
	
	@PostMapping(value = "")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Object> savePbxSystem(@RequestBody PbxSystemDto pbxSystemDto, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("Saving pbxServiceDto : {}", pbxSystemDto);
		pbxSystemService.savePbxSystem(pbxSystemDto);
		return ResponseEntity.status(HttpStatus.OK).body("");		
	}

	@PutMapping(value = "/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Object> updatePbxSystem(@RequestBody PbxSystemDto pbxSystemDto,@PathVariable("id") Long pbxSystemId, @AuthenticationPrincipal AMSPUser amspUser) {
		pbxSystemDto.setId(pbxSystemId);
		pbxSystemDto.setLogUpdatedBy(amspUser.getUsername());
		log.info("Updating pbxServiceDto : {}", pbxSystemDto);	
		pbxSystemService.updatePbxSystem(pbxSystemDto);
		return ResponseEntity.status(HttpStatus.OK).body(pbxSystemDto);		
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public  ResponseEntity<Object> deletePbxSystem(@PathVariable("id") Long idPbxSystem) {
		log.info("Deleted pbxSystem %s ", idPbxSystem);
	    pbxSystemService.deletePbxSystem(idPbxSystem);
		return ResponseEntity.status(HttpStatus.OK).body(idPbxSystem);
	}
	
	@GetMapping("/{id}/pbxcomponent")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchAllPbxComponentByPbxSystem(@PathVariable("id") Long idPbxSystem) {
		log.info("requesting for getting all pbxComponents for pbxystem %s",idPbxSystem);
		List<PbxComponentDto> response = pbxSystemService.fetchAllPbxComponentByPbxSystem(idPbxSystem);
        if (!response.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            log.warn("PbxComponents not found for pbxSystem: {}", idPbxSystem);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PbxComponents not found for pbxSystem:{} " + idPbxSystem);
        }
	}
	
	@GetMapping("/{id}/pbxspecialports")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchAllPbxSpecialPortsByPbxSystem(@PathVariable("id") Long idPbxSystem) {
		log.info("requesting for getting all pbxspecialports for pbxystem %s",idPbxSystem);
		List<PbxSpecialPortDto> response = pbxSystemService.fetchAllPbxSpecialPortsByPbxSystem(idPbxSystem);
        if (!response.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            log.warn("PbxSpecialPorts not found for pbxSystem: {}", idPbxSystem);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PbxSpecialPorts not found for pbxSystem:{} " + idPbxSystem);
        }
	}
	
	@GetMapping("/{id}/pbxports")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchAllPbxPortsByPbxSystem(@PathVariable("id") Long idPbxSystem) {
		log.info("requesting for getting all pbxports for pbxystem %s",idPbxSystem);
		List<PbxPortDto> response = pbxSystemService.fetchAllPbxPortsByPbxSystem(idPbxSystem);
        if (!response.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            log.warn("PbxPorts not found for pbxSystem: {}", idPbxSystem);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PbxPorts not found for pbxSystem:{} " + idPbxSystem);
        }
	}
	
	@GetMapping("/bysiteid/{siteId}")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchAllPbxSystemBySiteId(@PathVariable("siteId") Long siteId) {
		log.info("requesting for getting all pbxsystem for given siteId: {}", siteId);
		List<PbxSystemSiteDto> response = pbxSystemService.fetchAllPbxSystemBySiteId(siteId);
        if (!response.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            log.warn("PbxSystem not found for given siteId: {}", siteId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PbxSystem not found for given siteId:{} " + siteId);
        }
	}
}