package com.avaya.amsp.masterdata.service.iface;

import com.avaya.amsp.masterdata.dtos.CalendarClusterMappingDto;
import com.avaya.amsp.masterdata.dtos.CalendarClusterMappingDto;

public interface CalendarClusterMappingServiceIface {
    void createCalendarClusterMapping(CalendarClusterMappingDto calendarClusterMappingDto);
    CalendarClusterMappingDto getCalendarClusterMapping(Long mappingId);
    void deleteCalendarClusterMapping(Long mappingId);
}
