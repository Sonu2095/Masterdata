package com.avaya.amsp.masterdata.repo;

import com.avaya.amsp.domain.Calendar;
import com.avaya.amsp.domain.CalendarWorkWeek;
import com.avaya.amsp.domain.enums.CalendarEnum.Weekday;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CalendarWorkWeekRepository extends JpaRepository<CalendarWorkWeek, Long> {
    List<CalendarWorkWeek> findByCalendar_CalendarKey(String calendarKey);
    Optional<CalendarWorkWeek> findByCalendarAndWeekdayKey(Calendar calendar, Weekday weekdayKey);
    public void deleteByCalendarAndWeekdayKeyNotIn(Calendar calendar, Set<Weekday> weekdays);
    boolean existsByCalendarAndWeekdayKey(Calendar calendar, Weekday weekday);

}
