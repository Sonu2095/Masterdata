package com.avaya.amsp.masterdata.rest;

import com.avaya.amsp.masterdata.dtos.CalendarWorkWeekDto;
import com.avaya.amsp.masterdata.service.iface.CalendarWorkWeekServiceIface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/calendars/{calendarKey}/workweeks")
public class CalendarWorkWeekController {

    @Autowired
    private CalendarWorkWeekServiceIface workWeekService;

    @GetMapping
	@PreAuthorize("hasAnyRole('TK_P','AVAYA_ADMIN','TK_SV')")
    public ResponseEntity<List<CalendarWorkWeekDto>> getAllWorkWeeks(@PathVariable String calendarKey) {
        try {
            return ResponseEntity.ok(workWeekService.getAllWorkWeeksByCalendar(calendarKey));
        } catch (IllegalArgumentException e) {
            // Handle specific exception related to calendar key
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // or a custom error message
        }
    }

    @PostMapping
	@PreAuthorize("hasAnyRole('BOSCH_USER','BOSCH_ADMIN','AVAYA_ADMIN','AVAYA_HOTLINE','AVAYA_OPS')")
    public ResponseEntity<?> createWorkWeeks(@PathVariable String calendarKey, @RequestBody CalendarWorkWeekDto dto) {
        try {
        	String message = workWeekService.createWorkWeeks(dto);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            // Handle exception for invalid data (e.g., invalid weekday keys)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // or a custom error message
        }
    }
    
    @DeleteMapping("/{workWeekId}")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_SV')")
    public ResponseEntity<Void> deleteWorkWeek(@PathVariable Long workWeekId) {
        try {
            workWeekService.deleteWorkWeek(workWeekId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // Handle exception if work week not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
