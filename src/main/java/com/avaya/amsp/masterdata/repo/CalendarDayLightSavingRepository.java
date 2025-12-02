package com.avaya.amsp.masterdata.repo;

import com.avaya.amsp.domain.CalendarDayLightSaving;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CalendarDayLightSavingRepository extends JpaRepository<CalendarDayLightSaving, Long> {
    List<CalendarDayLightSaving> findByCalendar_CalendarKey(String calendarKey);

    Optional<CalendarDayLightSaving> findByCalendar_CalendarKeyAndDstYear(String calendarKey, Integer dstYear);
}
