package com.avaya.amsp.masterdata.service.iface;

import com.avaya.amsp.masterdata.dtos.CalendarHolidaysDto;
import java.util.List;

public interface CalendarHolidaysServiceIface {
    List<CalendarHolidaysDto> getAllHolidaysByCalendar(String calendarKey);
    CalendarHolidaysDto createHoliday(CalendarHolidaysDto dto, String userName);
    List<String> getHolidayNamesByCalendarKey(String calendarKey);
    void deleteHoliday(Long holidayId);
    public int copyHolidaysToCalendar(List<Long> holidayIds, String destinationCalendarKey);
    public CalendarHolidaysDto updateHoliday(Long holidayId, CalendarHolidaysDto dto, String userName);

}
