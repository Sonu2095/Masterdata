package com.avaya.amsp.masterdata.service;

import com.avaya.amsp.domain.Calendar;
import com.avaya.amsp.domain.CalendarWorkWeek;
import com.avaya.amsp.domain.enums.CalendarEnum.Weekday;
import com.avaya.amsp.masterdata.annotation.AuditLog;
import com.avaya.amsp.masterdata.dtos.CalendarWorkWeekDto;
import com.avaya.amsp.masterdata.repo.CalendarRepository;
import com.avaya.amsp.masterdata.repo.CalendarWorkWeekRepository;
import com.avaya.amsp.masterdata.service.iface.CalendarWorkWeekServiceIface;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CalendarWorkWeekService implements CalendarWorkWeekServiceIface {

    @Autowired
    private CalendarWorkWeekRepository workWeekRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Override
    public List<CalendarWorkWeekDto> getAllWorkWeeksByCalendar(String calendarKey) {
        // Fetch the associated calendar
        Calendar calendar = calendarRepository.findById(calendarKey)
                .orElseThrow(() -> new IllegalArgumentException("Invalid calendar key: " + calendarKey));

        // Fetch all work weeks associated with the calendar key
        List<CalendarWorkWeek> workWeeks = workWeekRepository.findByCalendar_CalendarKey(calendarKey);

        // Map to hold a single DTO for the calendar
        CalendarWorkWeekDto dto = new CalendarWorkWeekDto();
        dto.setCalendarKey(calendarKey);

        // Set default work times (if not already set in the calendar)
        dto.setWorkTimeStartFromString(
            calendar.getWorkTimeStart() != null
                ? calendar.getWorkTimeStart().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                : "09:00:00"
        );
        dto.setWorkTimeEndFromString(
            calendar.getWorkTimeEnd() != null
                ? calendar.getWorkTimeEnd().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                : "19:00:00"
        );
        
        dto.setRemark(calendar.getWorkweekRemark());

        // Combine all weekdays from the workWeeks into the weekdayKeys list
        List<String> weekdayKeys = workWeeks.stream()
            .map(workWeek -> workWeek.getWeekdayKey().name()) // Assuming Weekday is an Enum
            .collect(Collectors.toList());

        dto.setWeekdayKeys(weekdayKeys);

        // Return the list with a single CalendarWorkWeekDto (since we only need one entry for the calendar)
        return Collections.singletonList(dto);
    }

    @Transactional
    @Override
    @AuditLog(action = "update",entity = "CalendarWorkWeek",functionality = "Update Workweek to Calendar")
    public String createWorkWeeks(CalendarWorkWeekDto dto) {
        // Fetch calendar by calendar key
        Calendar calendar = calendarRepository.findById(dto.getCalendarKey())
                .orElseThrow(() -> new IllegalArgumentException("Invalid calendar key: " + dto.getCalendarKey()));

        // If work time start and end are provided, update the calendar table
        if (dto.getWorkTimeStart() != null) {
            calendar.setWorkTimeStart(dto.getWorkTimeStart());
        }
        if (dto.getWorkTimeEnd() != null) {
            calendar.setWorkTimeEnd(dto.getWorkTimeEnd());
        }
        
        calendar.setWorkweekRemark(dto.getRemark());
        // Save the updated calendar to store the work times
        calendarRepository.save(calendar);

        // Create a list of the weekdays in the incoming request
        Set<Weekday> weekdaysFromRequest = dto.getWeekdayKeys().stream()
                .map(weekday -> Weekday.valueOf(StringUtils.capitalize(weekday.toLowerCase())))
                .collect(Collectors.toSet());

        // Delete the work weeks that are not in the incoming request
        workWeekRepository.deleteByCalendarAndWeekdayKeyNotIn(calendar, weekdaysFromRequest);

        // Prepare a list to store new work week entries
        List<CalendarWorkWeek> workWeeksToSave = new ArrayList<>();

        // Loop over each weekday and create work week entries
        for (String weekday : dto.getWeekdayKeys()) {
            try {
                // Format the weekday and convert to enum
                String formattedWeekday = StringUtils.capitalize(weekday.toLowerCase());
                Weekday weekdayEnum = Weekday.valueOf(formattedWeekday);

                // Check if the work week already exists for this calendar and weekday
                boolean exists = workWeekRepository.existsByCalendarAndWeekdayKey(calendar, weekdayEnum);
                if (!exists) {
                    // Create a new work week entry if it doesn't exist
                    CalendarWorkWeek workWeek = new CalendarWorkWeek();
                    workWeek.setCalendar(calendar);
                    workWeek.setWeekdayKey(weekdayEnum);
                    workWeeksToSave.add(workWeek);
                }

            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid weekday key: " + weekday, e);
            }
        }
        String workWeek = "";
        // Save the new work weeks to the database if any
        if (!workWeeksToSave.isEmpty()) {
            workWeekRepository.saveAll(workWeeksToSave);
            workWeek = "Work weeks details saved";
        }

        // Return the saved work weeks as DTOs
        return workWeek;
        //return workWeeksToSave.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public void deleteWorkWeek(Long workWeekId) {
        if (workWeekRepository.existsById(workWeekId)) {
            workWeekRepository.deleteById(workWeekId);
        } else {
            throw new IllegalArgumentException("Work week with ID " + workWeekId + " not found.");
        }
    }

    private CalendarWorkWeekDto convertToDto(CalendarWorkWeek workWeek) {
        CalendarWorkWeekDto dto = new CalendarWorkWeekDto();
        dto.setIdWorkweek(workWeek.getIdWorkweek());
        dto.setCalendarKey(workWeek.getCalendar().getCalendarKey());
        dto.setWeekdayKeys(List.of(workWeek.getWeekdayKey().name())); // Convert Enum to String
        return dto;
    }
}
