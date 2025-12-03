package com.avaya.amsp.masterdata.rest;

import java.util.ArrayList;
import java.util.List;

import com.avaya.amsp.masterdata.dtos.*;
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

import com.avaya.amsp.masterdata.service.iface.PbxClusterServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1/pbxcluster")
public class PbxClusterController {
	@Autowired
	private PbxClusterServiceIface pbxClusterService;

	@GetMapping("")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchPbxControllers() {
		log.info("requesting for getting all pbxControllers");
		List<PbxClusterDto> response = pbxClusterService.fetchAllPbxClusters();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/{id}/numberrange")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchPbxNumberRange(@PathVariable("id") int idPbxCluster) {

		log.info("requesting for fetching pbx number range by pbx cluster");
		List<PbxNumberRangeDto> response = new ArrayList<>();
		response = pbxClusterService.fetchNumberRangeByPbxCluster(idPbxCluster);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/{id}/numberlocks")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchAllNumberLockByPbxCluser(@PathVariable("id") long idPbxCluster) {
		log.info("requesting for getting all number lock by pbxCluster {}", idPbxCluster);
		List<PbxNumberLockDto> response = pbxClusterService.fetchAllNumberLockByPbxCluser(idPbxCluster);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/{id}/pbxsystems")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchPbxSystemByPbxCluser(@PathVariable("id") long idPbxCluster) {
		log.info("requesting for getting all pbx-systems by pbxCluster {}", idPbxCluster);
		List<PbxSystemDto> response = pbxClusterService.fetchAllPbxSystemByPbxCluster(idPbxCluster);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PostMapping(value = "")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<Object> savePbxCluster(@RequestBody PbxClusterDto pbxClusterDto, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("Saving pbxCluster : {}", pbxClusterDto);
		pbxClusterDto.setLogCreatedBy(amspUser.getUsername());
		long id = pbxClusterService.savePbxCluster(pbxClusterDto);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@PutMapping(value = "")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<PbxClusterDto> updatePbxCluster(@RequestBody PbxClusterDto pbxClusterDto, @AuthenticationPrincipal AMSPUser amspUser) {
		log.info("Updating pbxCluster : {}", pbxClusterDto);
		pbxClusterDto.setLogUpdatedBy(amspUser.getUsername());
		pbxClusterDto = pbxClusterService.updatePbxCluster(pbxClusterDto);
		return ResponseEntity.status(HttpStatus.OK).body(pbxClusterDto);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN')")
	public ResponseEntity<String> deletePbxClusters(@PathVariable("id") Long pbxClusterId) {

		log.info("request received to delete pbx cluster {}", pbxClusterId);
		pbxClusterService.deletePbxClusters(pbxClusterId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
	}
	
	@GetMapping("/{id}/physicalpbx")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchAllPhysicalPbxbyPbxCluster(@PathVariable("id") long idPbxCluster) {
		log.info("requesting for getting all pbx-systems by pbxCluster {}",idPbxCluster);
		List<PhysicalPbxDto> response = pbxClusterService.fetchAllPhysicalPbxbyPbxCluster(idPbxCluster);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/{areaCode}")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> fetchPbxByAreaCode(@PathVariable("areaCode") String areaCode) {
		log.info("fetching PBX cluster for area code {}",areaCode);
 		PbxWrapper response = pbxClusterService.fetchPbxByAreaCode(areaCode);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}