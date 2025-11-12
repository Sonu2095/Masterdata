package com.avaya.amsp.masterdata.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CalendarDto {
    private String calendarKey;

    @Schema(description = "The starting year of the calendar", example = "2023")
    private int yearFrom;

    @Schema(description = "The ending year of the calendar", example = "2025")
    private int yearTo;

    private String description;
    
    // Use the shared ClockChangeBase enum
    @Schema(description = "Clock change base", example = "US", allowableValues = "DE, US, OTHERS")
    private String clockChangeBase;
    
}
	


