package com.avaya.amsp.masterdata.service.iface;

import com.avaya.amsp.masterdata.dtos.CalendarWorkWeekDto;

import java.util.List;

public interface CalendarWorkWeekServiceIface {

    /**
     * Get all work weeks for a specific calendar.
     */
    List<CalendarWorkWeekDto> getAllWorkWeeksByCalendar(String calendarKey);

    /**
     * Create new work week entries for a specific calendar.
     */
    String createWorkWeeks(CalendarWorkWeekDto dto);

    /**
     * Delete a specific work week by its ID.
     */
    void deleteWorkWeek(Long workWeekId);
}
