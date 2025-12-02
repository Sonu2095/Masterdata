package com.avaya.amsp.masterdata.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.avaya.amsp.masterdata.dtos.CalendarHolidaysDto;
import com.avaya.amsp.masterdata.service.iface.CalendarHolidaysServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import java.util.List;

@RestController
@RequestMapping("/v1/calendars/{calendarKey}/holidays")
public class CalendarHolidaysController {

    @Autowired
    private CalendarHolidaysServiceIface holidaysService;

    @GetMapping
	@PreAuthorize("hasAnyRole('TK_P','AVAYA_ADMIN','TK_SV')")
    public ResponseEntity<List<CalendarHolidaysDto>> getAllHolidays(@PathVariable String calendarKey) {
    	try {
            return ResponseEntity.ok(holidaysService.getAllHolidaysByCalendar(calendarKey));
    	}catch (IllegalArgumentException e) {
            // Handle specific exception related to calendar key
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // or a custom error message
        }
    }

    @GetMapping("/names")
	@PreAuthorize("hasAnyRole('TK_P','AVAYA_ADMIN','TK_SV')")
    public ResponseEntity<List<String>> getHolidayNamesByCalendarKey(@PathVariable String calendarKey) {
    	try {
            return ResponseEntity.ok(holidaysService.getHolidayNamesByCalendarKey(calendarKey));
    	}catch (IllegalArgumentException e) {
            // Handle specific exception related to calendar key
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // or a custom error message
        }
    }

    @PostMapping
   	@PreAuthorize("hasAnyRole('BOSCH_USER','BOSCH_ADMIN','AVAYA_ADMIN','AVAYA_HOTLINE','AVAYA_OPS')")
       public ResponseEntity<?> createHoliday(@PathVariable String calendarKey, @RequestBody CalendarHolidaysDto dto, @AuthenticationPrincipal AMSPUser amspUser) {
       	if (dto.getHolidayName() == null || dto.getHolidayName().trim().isEmpty() || dto.getHolidayDate() == null) {
       		return ResponseEntity.ok("Please fill out all required fields. ");
       	}
       	try {
               dto.setCalendarKey(calendarKey);
               return ResponseEntity.ok(holidaysService.createHoliday(dto, amspUser.getUsername()));
       		
       	}catch (Exception e) {
               // Handle exception for invalid data 
               return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // or a custom error message
           }
       }

    @PostMapping("/import")
	@PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_SV')")
    public ResponseEntity<String> copyHolidaysToCalendar(
            @PathVariable String calendarKey, 
            @RequestBody List<Long> holidayIds) {
        try {
            int copiedCount = holidaysService.copyHolidaysToCalendar(holidayIds, calendarKey);
            return ResponseEntity.ok("Successfully copied " + copiedCount + " holidays.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    @PutMapping("/{holidayId}")
    @PreAuthorize("hasAnyRole('AVAYA_ADMIN','TK_SV')")
    public ResponseEntity<CalendarHolidaysDto> updateHoliday(
            @PathVariable Long holidayId, 
            @RequestBody CalendarHolidaysDto holidayDto, @AuthenticationPrincipal AMSPUser amspUser) {
        try {
            CalendarHolidaysDto updatedHoliday = holidaysService.updateHoliday(holidayId, holidayDto, amspUser.getUsername());
            return ResponseEntity.ok(updatedHoliday);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); 
        }
    }
    
    @DeleteMapping("/{holidayId}")
	@PreAuthorize("hasAnyRole('TK_SV','AVAYA_ADMIN')")
    public ResponseEntity<String> deleteHoliday(@PathVariable Long holidayId) {
    	try {
            holidaysService.deleteHoliday(holidayId);
            return ResponseEntity.ok("Record is deleted");
    	}catch (IllegalArgumentException e) {
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


}
