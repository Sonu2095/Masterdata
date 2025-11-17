package com.avaya.amsp.masterdata.rest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.domain.enums.CalendarEnum.ClockChangeBase;
import com.avaya.amsp.domain.enums.CalendarEnum.Weekday;
import com.avaya.amsp.masterdata.dtos.CalendarDetailsDto;
import com.avaya.amsp.masterdata.dtos.CalendarDto;
import com.avaya.amsp.masterdata.dtos.ClusterDto;
import com.avaya.amsp.masterdata.dtos.ClustersToCalendarDto;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.service.iface.CalendarServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/calendars")
public class CalendarController {

	@Autowired
	private CalendarServiceIface calendarService;

	@GetMapping
	public ResponseEntity<List<CalendarDto>> getAllCalendars() {
		return ResponseEntity.ok(calendarService.getAllCalendars());
	}

	@GetMapping("/{calendarKey}")
	@PreAuthorize("hasAnyRole('TK_P','AVAYA_ADMIN','TK_SV')")
	public ResponseEntity<CalendarDto> getCalendarByKey(@PathVariable String calendarKey) {
		try {
			return ResponseEntity.ok(calendarService.getCalendarByKey(calendarKey));
		} catch (IllegalArgumentException e) {
			// Handle specific exception related to calendar key
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // or a custom error message
		}
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_SV')")
	public ResponseEntity<CalendarDto> createCalendar(@RequestBody CalendarDto dto,
			@AuthenticationPrincipal AMSPUser amspUser) {
		try {
			return ResponseEntity.ok(calendarService.createCalendar(dto, amspUser.getUsername()));
		} catch (IllegalArgumentException e) {
			// Handle exception for invalid data (e.g., invalid weekday keys)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // or a custom error message
		}
	}

	@PutMapping("/update")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_SV')")
	public ResponseEntity<String> updateCalendar(@RequestBody CalendarDto request,
			@AuthenticationPrincipal AMSPUser amspUser) {
		log.info("request received to update Calendar {}", request);
		String updateCalendarMsg = calendarService.updateCalendar(request, amspUser.getUsername());
		return ResponseEntity.status(HttpStatus.OK).body(updateCalendarMsg);
	}

	@DeleteMapping("/{calendarKey}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_SV')")
	public ResponseEntity<Void> deleteCalendar(@PathVariable String calendarKey) {

		try {
			calendarService.deleteCalendar(calendarKey);
			return ResponseEntity.noContent().build();

		} catch (IllegalArgumentException e) {
			// Handle exception if work week not found
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

	@PostMapping("/{calendarKey}/clusters")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_SV')")
	public ResponseEntity<String> addClustersCalendar(@PathVariable("calendarKey") String calendarKey,
			@RequestBody @Valid ClustersToCalendarDto clusters) {
		log.info("request received to add clusters to calendar {}. cluster {}", calendarKey, clusters);
		calendarService.addClustersToCalendar(calendarKey, clusters);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@DeleteMapping("/{calendarKey}/clusters")
	@PreAuthorize("hasAnyRole('TK_SV','AVAYA_ADMIN')")
	public ResponseEntity<String> removeClustersFromCalendar(@PathVariable String calendarKey,
			@RequestBody List<Long> clusterIds) {
		try {
			calendarService.removeClustersFromCalendar(calendarKey, clusterIds);
			return ResponseEntity.ok("Clusters removed successfully from calendar: " + calendarKey);
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
		}
	}

	// Endpoint to update clusters for a specific calendar
	@PutMapping("/{oldCalendarKey}/updateClusters/{newCalendarKey}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_SV')")
	public ResponseEntity<String> updateClustersForCalendar(@PathVariable String oldCalendarKey,
			@PathVariable String newCalendarKey, @RequestBody ClustersToCalendarDto clusters) {

		try {
			calendarService.updateClustersForCalendar(oldCalendarKey, newCalendarKey, clusters);
			return ResponseEntity.status(HttpStatus.OK)
					.body("Clusters updated successfully for calendar: " + newCalendarKey);
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An unexpected error occurred: " + e.getMessage());
		}
	}

	// Endpoint to fetch clusters that are not assigned to any calendar
	@GetMapping("/unassignedClusters")
	@PreAuthorize("hasAnyRole('TK_P','AVAYA_ADMIN','TK_SV')")
	public ResponseEntity<List<ClusterDto>> getClustersNotAssignedToAnyCalendar() {
		try {
			List<ClusterDto> unassignedClusters = calendarService.getClustersNotAssignedToAnyCalendar();
			return ResponseEntity.status(HttpStatus.OK).body(unassignedClusters);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	// Fetch all ClockChangeBase enum values
	@GetMapping("/enums/clockchangebase")
	@PreAuthorize("hasAnyRole('TK_P','AVAYA_ADMIN','TK_SV')")
	public List<String> getAllClockChangeBase() {
		return Arrays.stream(ClockChangeBase.values()).map(ClockChangeBase::name).toList(); // or use name() if you want the enum
																					// names
	}

	// Fetch all Weekday enum values
	@GetMapping("/enums/weekday")
	@PreAuthorize("hasAnyRole('TK_P','AVAYA_ADMIN','TK_SV')")
	public List<String> getAllWeekdays() {
		return Arrays.stream(Weekday.values()).map(Weekday::name).toList(); // or use name() if you want the enum names
				
	}

	@GetMapping("/{calendarKey}/clusters")
	@PreAuthorize("hasAnyRole('TK_P','AVAYA_ADMIN','TK_SV')")
	public ResponseEntity<List<ClusterDto>> getClustersByCalendar(@PathVariable String calendarKey) {
		try {
			// Fetch the clusters for the given calendar key
			List<ClusterDto> clusters = calendarService.fetchClustersByCalendar(calendarKey);

			// Return response with 200 OK status and the list of clusters
			return ResponseEntity.status(HttpStatus.OK).body(clusters);
		} catch (Exception e) {
			// Return an error response if something goes wrong (e.g., calendar not found)
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@GetMapping("/details")
	@PreAuthorize("hasAnyRole('BOSCH_USER','BOSCH_ADMIN','AVAYA_ADMIN','AVAYA_HOTLINE','AVAYA_OPS')")
	public ResponseEntity<CalendarDetailsDto> getCalendarDetails(@RequestParam Integer clusterId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

		CalendarDetailsDto response = calendarService.getCalendarDetails(clusterId, date);
		if (response == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		return ResponseEntity.ok(response);
	}

}
