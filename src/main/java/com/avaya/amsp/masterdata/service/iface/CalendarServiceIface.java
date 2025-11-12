package com.avaya.amsp.masterdata.service.iface;

import java.time.LocalDate;
import java.util.List;

import com.avaya.amsp.masterdata.dtos.CalendarDetailsDto;
import com.avaya.amsp.masterdata.dtos.CalendarDto;
import com.avaya.amsp.masterdata.dtos.ClusterDto;
import com.avaya.amsp.masterdata.dtos.ClustersToCalendarDto;

public interface CalendarServiceIface {

	List<CalendarDto> getAllCalendars();

	CalendarDto getCalendarByKey(String calendarKey);

	public CalendarDto createCalendar(CalendarDto calendarDto, String userName);

	public void addClustersToCalendar(String calendarKey, ClustersToCalendarDto clusters);

	public void removeClustersFromCalendar(String calendarKey, List<Long> clusterIds);

	public void updateClustersForCalendar(String oldCalendarKey, String newCalendarKey, ClustersToCalendarDto clusters);

	public List<ClusterDto> getClustersNotAssignedToAnyCalendar();

	public List<ClusterDto> fetchClustersByCalendar(String calendarKey);

	boolean deleteCalendar(String calendarKey);

	public CalendarDetailsDto getCalendarDetails(Integer clusterId, LocalDate date);

	public String updateCalendar(CalendarDto dto, String username);

}
