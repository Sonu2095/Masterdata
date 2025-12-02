package com.avaya.amsp.masterdata.dtos;

import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalendarDetailsDto {
    private Integer clusterId;
    private String calendarKey;
    private List<String> workingDays;
    private LocalTime workTimeStart;
    private LocalTime workTimeEnd;
    private List<CalendarHolidaysDto> holidays;
}
