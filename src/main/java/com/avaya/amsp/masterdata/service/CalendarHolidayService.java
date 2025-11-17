package com.avaya.amsp.masterdata.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avaya.amsp.domain.Calendar;
import com.avaya.amsp.domain.CalendarHolidays;
import com.avaya.amsp.masterdata.annotation.AuditLog;
import com.avaya.amsp.masterdata.dtos.CalendarHolidaysDto;
import com.avaya.amsp.masterdata.repo.CalendarHolidaysRepository;
import com.avaya.amsp.masterdata.repo.CalendarRepository;
import com.avaya.amsp.masterdata.service.iface.CalendarHolidaysServiceIface;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CalendarHolidayService implements CalendarHolidaysServiceIface {

	@Autowired
	private CalendarHolidaysRepository holidaysRepository;

	@Autowired
	private CalendarRepository calendarRepository;

	@Override
	public List<CalendarHolidaysDto> getAllHolidaysByCalendar(String calendarKey) {
		List<CalendarHolidays> holidays = holidaysRepository.findByCalendar_CalendarKey(calendarKey);
		return holidays.stream().map(this::convertToDtoF).collect(Collectors.toList());
	}

	@Override
	@AuditLog(action = "Insert",entity = "CalendarHolidays",functionality = "Add New CalendarHolidays")
	public CalendarHolidaysDto createHoliday(CalendarHolidaysDto dto, String userName) {
		// Fetch the calendar entity using the calendar key
		Calendar calendar = calendarRepository.findByCalendarKey(dto.getCalendarKey())
				.orElseThrow(() -> new IllegalArgumentException("Invalid calendar key: " + dto.getCalendarKey()));

		// Check if a holiday with the same name already exists
		CalendarHolidays existingHoliday = holidaysRepository
				.findByCalendar_CalendarKeyAndHolidayName(dto.getCalendarKey(), dto.getHolidayName());

		if (existingHoliday != null) {
			if (existingHoliday.getValidEachYear()) {
				// If the existing holiday is marked as valid each year, prevent duplication
				log.info("Holiday '" + dto.getHolidayName()
						+ "' is already marked as recurring every year for this calendar.");
				throw new IllegalStateException("Holiday '" + dto.getHolidayName()
						+ "' is already marked as recurring every year for this calendar.");
			}
			// Otherwise, update the existing entry
			existingHoliday.setValidEachYear(dto.getValidEachYear());
			existingHoliday.setHolidayDate(dto.getHolidayDate());
			existingHoliday.setRemark(dto.getRemark());

			// Save the updated holiday
			CalendarHolidays updatedHoliday = holidaysRepository.save(existingHoliday);
			return convertToDto(updatedHoliday);
		}

		// If the holiday doesn't exist, create a new one
		CalendarHolidays holiday = new CalendarHolidays();
		holiday.setCalendar(calendar);
		holiday.setHolidayName(dto.getHolidayName());
		holiday.setValidEachYear(dto.getValidEachYear());
		holiday.setHolidayDate(dto.getHolidayDate());
		holiday.setRemark(dto.getRemark());
		holiday.setLogCreatedBy(userName);
		holiday.setLogCreatedOn(LocalDateTime.now());
		holiday.setLogUpdatedBy(userName);
		holiday.setLogUpdatedOn(LocalDateTime.now());
		// Save the new holiday
		CalendarHolidays savedHoliday = holidaysRepository.save(holiday);
		return convertToDto(savedHoliday);
	}

	@Override
	public List<String> getHolidayNamesByCalendarKey(String calendarKey) {
		return holidaysRepository.findHolidayNamesByCalendarKey(calendarKey);
	}

	@Override
	@AuditLog(action = "delete",entity = "CalendarHolidays",functionality = "delete existing CalendarHolidays")
	public void deleteHoliday(Long holidayId) {
		holidaysRepository.deleteById(holidayId);
	}

	// Helper method to convert CalendarHolidays entity to DTO
	private CalendarHolidaysDto convertToDto(CalendarHolidays entity) {
		CalendarHolidaysDto dto = new CalendarHolidaysDto();

		dto.setCalendarKey(entity.getCalendar().getCalendarKey());
		dto.setHolidayName(entity.getHolidayName());
		dto.setValidEachYear(entity.getValidEachYear());
		dto.setHolidayDate(entity.getHolidayDate()); // Use LocalDate directly
		dto.setRemark(entity.getRemark());
		return dto;
	}

	private CalendarHolidaysDto convertToDtoF(CalendarHolidays entity) {
		CalendarHolidaysDto dto = new CalendarHolidaysDto();
		dto.setIdHoliday(entity.getIdHoliday());
		dto.setCalendarKey(entity.getCalendar().getCalendarKey());
		dto.setHolidayName(entity.getHolidayName());
		dto.setValidEachYear(entity.getValidEachYear());
		dto.setHolidayDate(entity.getHolidayDate()); // Use LocalDate directly
		dto.setRemark(entity.getRemark());
		return dto;
	}

	@Transactional
	@Override
	@AuditLog(action = "update",entity = "CalendarHolidays",functionality = "update existing CalendarHolidays")
	public CalendarHolidaysDto updateHoliday(Long holidayId, CalendarHolidaysDto dto, String userName) {
		CalendarHolidays holiday = holidaysRepository.findById(holidayId)
				.orElseThrow(() -> new IllegalArgumentException("Holiday not found with ID: " + holidayId));

		holiday.setHolidayName(dto.getHolidayName());
		holiday.setValidEachYear(dto.getValidEachYear());
		holiday.setHolidayDate(dto.getHolidayDate());
		holiday.setRemark(dto.getRemark());
		holiday.setLogUpdatedBy(userName);
		holiday.setLogUpdatedOn(LocalDateTime.now());

		CalendarHolidays updatedHoliday = holidaysRepository.save(holiday);
		return convertToDto(updatedHoliday);
	}

	@Transactional
	@Override
	public int copyHolidaysToCalendar(List<Long> holidayIds, String destinationCalendarKey) {
		// Fetch destination calendar
		Calendar destinationCalendar = calendarRepository.findByCalendarKey(destinationCalendarKey).orElseThrow(
				() -> new IllegalArgumentException("Destination calendar not found: " + destinationCalendarKey));

		// Fetch holidays by IDs
		List<CalendarHolidays> selectedHolidays = holidaysRepository.findAllById(holidayIds);

		if (selectedHolidays.isEmpty()) {
			throw new IllegalArgumentException("No holidays found for the given IDs.");
		}

		// Copy and save holidays for the new calendar
		List<CalendarHolidays> newHolidays = selectedHolidays.stream().map(holiday -> {
			if (holidaysRepository.existsByCalendar_CalendarKeyAndHolidayNameAndHolidayDate(destinationCalendarKey,
					holiday.getHolidayName(), holiday.getHolidayDate())) {
				log.warn("Holiday '" + holiday.getHolidayName() + "' on " + holiday.getHolidayDate()
						+ " already exists in the destination calendar.");
				return null; // Skip if it already exists
			}

			CalendarHolidays newHoliday = new CalendarHolidays();
			newHoliday.setCalendar(destinationCalendar);
			newHoliday.setHolidayName(holiday.getHolidayName());
			newHoliday.setValidEachYear(holiday.getValidEachYear());
			newHoliday.setHolidayDate(holiday.getHolidayDate());
			newHoliday.setRemark(holiday.getRemark());
			return newHoliday;
		}).filter(holiday -> holiday != null) // Filter out null holidays
				.collect(Collectors.toList());

		// Save copied holidays
		if (!newHolidays.isEmpty()) {
			holidaysRepository.importHolidaysToCalenday(newHolidays);
			log.info(newHolidays.size() + " holidays copied to calendar: " + destinationCalendarKey);
		} else {
			log.warn("No holidays copied to the destination calendar.");
		}

		return newHolidays.size(); // Return the correct count of copied holidays
	}

}
