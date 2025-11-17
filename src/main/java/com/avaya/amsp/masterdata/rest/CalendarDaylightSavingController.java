package com.avaya.amsp.masterdata.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.avaya.amsp.masterdata.dtos.CalendarDayLightSavingDto;
import com.avaya.amsp.masterdata.service.iface.CalendarDayLightSavingServiceIface;

import java.util.List;

@RestController
@RequestMapping("/v1/calendars/{calendarKey}/daylightsavings")
public class CalendarDaylightSavingController {

    @Autowired
    private CalendarDayLightSavingServiceIface daylightSavingService;

    /**
     * Get all daylight saving records for a particular calendar
     * @param calendarKey The calendar key to fetch records for
     * @return List of daylight saving records
     */
    @GetMapping
	@PreAuthorize("hasAnyRole('TK_P','AVAYA_ADMIN','TK_SV')")
    public ResponseEntity<List<CalendarDayLightSavingDto>> getAllDaylightSavings(
            @PathVariable String calendarKey) {
    	try {
            return ResponseEntity.ok(daylightSavingService.getAllDaylightSavingsByCalendar(calendarKey));
    	} catch (IllegalArgumentException e) {
            // Handle specific exception related to calendar key
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // or a custom error message
        }
    }

    @GetMapping("/{dstYear}")
	@PreAuthorize("hasAnyRole('TK_P','AVAYA_ADMIN','TK_SV')")
    public ResponseEntity<CalendarDayLightSavingDto> getDaylightSavingByYear(
            @PathVariable String calendarKey, 
            @PathVariable int dstYear) {
        
        CalendarDayLightSavingDto daylightSaving = daylightSavingService.getDaylightSavingByYear(calendarKey, dstYear);
        return ResponseEntity.ok(daylightSaving);
    }

    /**
     * Create a new daylight saving record for a particular calendar
     * @param calendarKey The calendar key for which to create the record
     * @param dto The Daylight Saving Data Transfer Object (DTO)
     * @return The created daylight saving record
     */
    @PostMapping
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_SV')")
    public ResponseEntity<CalendarDayLightSavingDto> createDaylightSaving(
            @PathVariable String calendarKey,
            @RequestBody CalendarDayLightSavingDto dto) {
    	try {
            dto.setCalendarKey(calendarKey); // Attach the calendar key to the DTO
            return ResponseEntity.ok(daylightSavingService.createDaylightSaving(dto));
    		
    	}catch (IllegalArgumentException e) {
            // Handle exception for invalid data 
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // or a custom error message
        }

    }
    

    /**
     * Delete a daylight saving record by its ID
     * @param daylightSavingId The ID of the daylight saving record to delete
     * @return No content status after deletion
     */
    @DeleteMapping("/{daylightSavingId}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_SV')")
    public ResponseEntity<String> deleteDaylightSaving(
    		@PathVariable String calendarKey,
            @PathVariable Long daylightSavingId) {
    	try {
            daylightSavingService.deleteDaylightSaving(daylightSavingId);
            return ResponseEntity.ok("Deleted dayLighSaving for " + calendarKey);

    	}catch (IllegalArgumentException e) {
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }
}
