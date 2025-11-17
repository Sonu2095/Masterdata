package com.avaya.amsp.masterdata.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avaya.amsp.domain.Calendar;
import com.avaya.amsp.domain.CalendarClusterMapping;
import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.masterdata.annotation.AuditLog;

public interface CalendarClusterMappingRepository extends JpaRepository<CalendarClusterMapping, Long> {
    List<CalendarClusterMapping> findByCalendarAndClusterIdIn(Calendar calendar, List<Long> clusterIds);
    List<CalendarClusterMapping> findByClusterIn(List<ClusterItem> clusters);
    Optional<CalendarClusterMapping> findByCluster_Id(Integer clusterId);
    
    @AuditLog(action = "Insert",entity = "CalendarClusterMapping",functionality = "Add Clusters to Calender")
	default List<CalendarClusterMapping> addClusterToCalender(List<CalendarClusterMapping> listCalendarClusterMapping){
		return saveAll(listCalendarClusterMapping);
	}
    
	@Query(value = "select CCM from CalendarClusterMapping  CCM where  CCM.calendar.calendarKey=:calenderKey AND CCM.cluster.id in :clusterIds")
	public List<CalendarClusterMapping> findByCalendarIdAndClusterIds(String calenderKey, List<Long> clusterIds);

}
