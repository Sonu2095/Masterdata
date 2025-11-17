package com.avaya.amsp.masterdata.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.avaya.amsp.domain.CalendarHolidays;
import com.avaya.amsp.domain.CalendarWorkWeek;
import com.avaya.amsp.masterdata.annotation.AuditLog;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalendarHolidaysRepository extends JpaRepository<CalendarHolidays, Long> {
    List<CalendarHolidays> findByCalendar_CalendarKey(String calendarKey);

    CalendarHolidays findByCalendar_CalendarKeyAndHolidayName(String calendarKey, String holidayName);

    @Query("SELECT ch.holidayName FROM CalendarHolidays ch WHERE ch.calendar.calendarKey = :calendarKey")
    List<String> findHolidayNamesByCalendarKey(@Param("calendarKey") String calendarKey);
    
    boolean existsByCalendar_CalendarKeyAndHolidayNameAndHolidayDate(String calendarKey, String holidayName, LocalDate holidayDate);

    List<CalendarHolidays> findByCalendar_CalendarKeyAndHolidayDateBetween(String calendarKey, LocalDate startDate, LocalDate endDate);
    
    @AuditLog(action = "Insert",entity = "CalendarHolidays",functionality = "import Holidays to calender")
	default List<CalendarHolidays> importHolidaysToCalenday(List<CalendarHolidays> listCalendarHolidays){
		return saveAll(listCalendarHolidays);
	}

}
