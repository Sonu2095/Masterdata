package com.avaya.amsp.masterdata.repo;

import com.avaya.amsp.domain.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CalendarRepository extends JpaRepository<Calendar, String> {
    Optional<Calendar> findByCalendarKey(String calendarKey);
}
