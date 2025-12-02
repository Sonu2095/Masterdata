package com.avaya.amsp.masterdata.service.iface;

import com.avaya.amsp.masterdata.dtos.CalendarDayLightSavingDto;
import java.util.List;

public interface CalendarDayLightSavingServiceIface {

    List<CalendarDayLightSavingDto> getAllDaylightSavingsByCalendar(String calendarKey);

    CalendarDayLightSavingDto createDaylightSaving(CalendarDayLightSavingDto dto);

    void deleteDaylightSaving(Long daylightSavingId);
    
    public CalendarDayLightSavingDto getDaylightSavingByYear(String calendarKey, int dstYear);

}
