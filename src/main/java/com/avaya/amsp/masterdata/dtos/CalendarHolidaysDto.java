package com.avaya.amsp.masterdata.dtos;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarHolidaysDto {
    private Long idHoliday;
    private String calendarKey;
    private String holidayName;
    private Boolean validEachYear;
 
    @JsonFormat(pattern = "yyyy-MM-dd")  // Ensure correct formatting during serialization/deserialization
    private LocalDate holidayDate;
    private String remark;
    
}
