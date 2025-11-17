package com.avaya.amsp.masterdata.service;

import com.avaya.amsp.domain.Calendar;
import com.avaya.amsp.domain.CalendarDayLightSaving;
import com.avaya.amsp.masterdata.dtos.CalendarDayLightSavingDto;
import com.avaya.amsp.masterdata.repo.CalendarDayLightSavingRepository;
import com.avaya.amsp.masterdata.repo.CalendarRepository;
import com.avaya.amsp.masterdata.service.iface.CalendarDayLightSavingServiceIface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CalendarDayLightSavingService implements CalendarDayLightSavingServiceIface {

    @Autowired
    private CalendarDayLightSavingRepository daylightSavingRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Override
    public List<CalendarDayLightSavingDto> getAllDaylightSavingsByCalendar(String calendarKey) {
        List<CalendarDayLightSaving> daylightSavings = daylightSavingRepository.findByCalendar_CalendarKey(calendarKey);
        return daylightSavings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CalendarDayLightSavingDto createDaylightSaving(CalendarDayLightSavingDto dto) {
        Calendar calendar = calendarRepository.findByCalendarKey(dto.getCalendarKey())
                .orElseThrow(() -> new IllegalArgumentException("Invalid calendar key: " + dto.getCalendarKey()));

        Optional<CalendarDayLightSaving> existingRecord = daylightSavingRepository.findByCalendar_CalendarKeyAndDstYear(dto.getCalendarKey(), dto.getDstYear());
        CalendarDayLightSaving daylightSaving = existingRecord.orElse(new CalendarDayLightSaving());

        daylightSaving.setCalendar(calendar);
        daylightSaving.setDstYear(dto.getDstYear());
        daylightSaving.setSwitchToSummerTime(dto.getSwitchToSummerTime());
        daylightSaving.setReturnToStandardTime(dto.getReturnToStandardTime());
        daylightSaving.setHourOffset(dto.getHourOffset());
        daylightSaving.setRemark(dto.getRemark());

        daylightSaving = daylightSavingRepository.save(daylightSaving);
        return convertToDto(daylightSaving);
    }

    @Override
    public void deleteDaylightSaving(Long daylightSavingId) {
        daylightSavingRepository.deleteById(daylightSavingId);
    }

    @Override
    public CalendarDayLightSavingDto getDaylightSavingByYear(String calendarKey, int dstYear) {
        return daylightSavingRepository.findByCalendar_CalendarKeyAndDstYear(calendarKey, dstYear)
                .map(this::convertToDto)
                .orElse(null);
    }

    private CalendarDayLightSavingDto convertToDto(CalendarDayLightSaving daylightSaving) {
        CalendarDayLightSavingDto dto = new CalendarDayLightSavingDto();
        dto.setIdDst(daylightSaving.getIdDst());
        dto.setCalendarKey(daylightSaving.getCalendar().getCalendarKey());
        dto.setDstYear(daylightSaving.getDstYear());
        dto.setSwitchToSummerTime(daylightSaving.getSwitchToSummerTime());
        dto.setReturnToStandardTime(daylightSaving.getReturnToStandardTime());
        dto.setHourOffset(daylightSaving.getHourOffset());
        dto.setRemark(daylightSaving.getRemark());
        return dto;
    }
}
