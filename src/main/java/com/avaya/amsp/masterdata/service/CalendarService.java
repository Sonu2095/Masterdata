package com.avaya.amsp.masterdata.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.Calendar;
import com.avaya.amsp.domain.CalendarClusterMapping;
import com.avaya.amsp.domain.CalendarHolidays;
import com.avaya.amsp.domain.CalendarWorkWeek;
import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.enums.CalendarEnum.ClockChangeBase;
import com.avaya.amsp.masterdata.annotation.AuditLog;
import com.avaya.amsp.masterdata.dtos.CalendarDetailsDto;
import com.avaya.amsp.masterdata.dtos.CalendarDto;
import com.avaya.amsp.masterdata.dtos.CalendarHolidaysDto;
import com.avaya.amsp.masterdata.dtos.ClusterDto;
import com.avaya.amsp.masterdata.dtos.ClustersToCalendarDto;
import com.avaya.amsp.masterdata.exceptions.InvalidCalendarException;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.CalendarClusterMappingRepository;
import com.avaya.amsp.masterdata.repo.CalendarHolidaysRepository;
import com.avaya.amsp.masterdata.repo.CalendarRepository;
import com.avaya.amsp.masterdata.repo.CalendarWorkWeekRepository;
import com.avaya.amsp.masterdata.repo.ClusterRepository;
import com.avaya.amsp.masterdata.service.iface.CalendarServiceIface;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CalendarService implements CalendarServiceIface {

	@Autowired
	private CalendarRepository calendarRepository;

	@Autowired
	private CalendarClusterMappingRepository calendarClusterRepo;

	@Autowired
	private CalendarWorkWeekRepository workWeekRepo;

	@Autowired
	private CalendarHolidaysRepository holidaysRepo;

	@Autowired
	ClusterRepository clusterRepo;

	@Autowired
	ModelMapper mapper;

	@Override
	public List<CalendarDto> getAllCalendars() {
		List<Calendar> calendars = calendarRepository.findAll();
		return calendars.stream().map(calendar -> {
			CalendarDto dto = new CalendarDto();
			dto.setCalendarKey(calendar.getCalendarKey());
			dto.setYearFrom(calendar.getYearFrom());
			dto.setYearTo(calendar.getYearTo());
			dto.setDescription(calendar.getDescription());
			dto.setClockChangeBase(calendar.getClockChangeBase() != null ? calendar.getClockChangeBase().name() : null); // Map
			return dto;
		}).collect(Collectors.toList());
	}

	@Override
	public CalendarDto getCalendarByKey(String calendarKey) {
		Optional<Calendar> calendarOpt = calendarRepository.findById(calendarKey);
		if (calendarOpt.isPresent()) {
			Calendar calendar = calendarOpt.get();
			CalendarDto dto = new CalendarDto();
			dto.setCalendarKey(calendar.getCalendarKey());
			dto.setYearFrom(calendar.getYearFrom());
			dto.setYearTo(calendar.getYearTo());
			dto.setDescription(calendar.getDescription());
			dto.setClockChangeBase(calendar.getClockChangeBase() != null ? calendar.getClockChangeBase().name() : null); // Map
			return dto;
		}
		return null; // Return null if not found
	}

	@Override
	@AuditLog(action = "Insert",entity = "Calendar",functionality = "Add New Calender")
	public CalendarDto createCalendar(CalendarDto calendarDto, String userName) {
		// Convert CalendarDto to Calendar entity
		Calendar calendar = new Calendar();
		calendar.setCalendarKey(calendarDto.getCalendarKey());
		calendar.setYearFrom(calendarDto.getYearFrom());
		calendar.setYearTo(calendarDto.getYearTo());
		calendar.setWorkTimeStart(LocalTime.of(9, 0, 0));
		calendar.setWorkTimeEnd(LocalTime.of(17, 0, 0));
		calendar.setDescription(calendarDto.getDescription());
		calendar.setLogCreatedBy(userName);
		calendar.setLogCreatedOn(LocalDateTime.now());
		calendar.setLogUpdatedBy(userName);
		calendar.setLogUpdatedOn(LocalDateTime.now());
		// Set the clockChangeBase from DTO
		if (calendarDto.getClockChangeBase() != null) {
			try {
				// Set the clock change base enum value from the string
				calendar.setClockChangeBase(ClockChangeBase.valueOf(calendarDto.getClockChangeBase()));
			} catch (IllegalArgumentException e) {
				log.error("Invalid clock change base: " + calendarDto.getClockChangeBase());
				throw new IllegalArgumentException("Invalid clock change base ");
			}
		}
		// Save calendar to database
		Calendar savedCalendar = calendarRepository.save(calendar);

		// Convert saved entity back to CalendarDto
		CalendarDto dto = new CalendarDto();
		dto.setCalendarKey(savedCalendar.getCalendarKey());
		dto.setYearFrom(savedCalendar.getYearFrom());
		dto.setYearTo(savedCalendar.getYearTo());
		dto.setDescription(savedCalendar.getDescription());
		dto.setClockChangeBase(savedCalendar.getClockChangeBase() != null ? savedCalendar.getClockChangeBase().name() : null); // Map
		return dto;
	}

	@Override
	@AuditLog(action = "Update",entity = "Calendar",functionality = "Update Existing New Calender")
	public String updateCalendar(CalendarDto calendarDto, String userName) {
		// Convert CalendarDto to Calendar entity
		String calendarUpdateMsg = "";
		Optional<Calendar> calendarOpt = calendarRepository.findById(calendarDto.getCalendarKey());
		if (calendarOpt.isPresent()) {
			Calendar calendar = calendarOpt.get();
			calendar.setCalendarKey(calendarDto.getCalendarKey());
			calendar.setYearFrom(calendarDto.getYearFrom());
			calendar.setYearTo(calendarDto.getYearTo());
			calendar.setWorkTimeStart(LocalTime.of(9, 0, 0));
			calendar.setWorkTimeEnd(LocalTime.of(17, 0, 0));
			calendar.setDescription(calendarDto.getDescription());
			calendar.setLogUpdatedBy(userName);
			calendar.setLogUpdatedOn(LocalDateTime.now());
			// Set the clockChangeBase from DTO
			if (calendarDto.getClockChangeBase() != null) {
				try {
					// Set the clock change base enum value from the string
					calendar.setClockChangeBase(ClockChangeBase.valueOf(calendarDto.getClockChangeBase()));
				} catch (IllegalArgumentException e) {
					log.error("Invalid clock change base: " + calendarDto.getClockChangeBase());
					throw new IllegalArgumentException("Invalid clock change base ");
				}
			}
			// Save calendar to database
			try {
				calendarRepository.save(calendar);
				calendarUpdateMsg = "Updated Successfully";
			} catch (Exception e) {
				calendarUpdateMsg = "Updation Failure";
			}
		} else {
			calendarUpdateMsg = "Calendar key not valid";
		}
		return calendarUpdateMsg;
	}

	@Override
	@AuditLog(action = "delete",entity = "Calendar",functionality = "delete existing New Calender")
	public boolean deleteCalendar(String calendarKey) {
		Optional<Calendar> calendarOpt = calendarRepository.findById(calendarKey);
		if (calendarOpt.isPresent()) {
			calendarRepository.delete(calendarOpt.get());
			return true;
		}
		return false;
	}

	@Override
	public void addClustersToCalendar(String calendarKey, ClustersToCalendarDto clusters) {
		log.info("Request received for adding clusters to calendar: {}", calendarKey);

		// Fetch the calendar by its key
		Optional<Calendar> calendarOpt = calendarRepository.findByCalendarKey(calendarKey);
		calendarOpt.ifPresentOrElse(calendar -> {
			// Get the list of clusters from the provided cluster IDs
			List<ClusterItem> clusterItems = clusterRepo.findAllById(clusters.getClusterIds());

			// Check if all provided clusters exist in the repository
			if (clusterItems.size() != clusters.getClusterIds().size()) {
				throw new IllegalArgumentException("Some of the clusters were not found");
			}

			// Check if any of the clusters are already assigned to another calendar
			List<CalendarClusterMapping> existingMappings = calendarClusterRepo.findByClusterIn(clusterItems);
			if (!existingMappings.isEmpty()) {
				// Filter out clusters that are already assigned to a calendar (i.e., are not
				// unique)
				List<ClusterItem> alreadyAssignedClusters = existingMappings.stream()
						.map(CalendarClusterMapping::getCluster).collect(Collectors.toList());

				List<Long> alreadyAssignedClusterIds = alreadyAssignedClusters.stream().map(ClusterItem::getId)
						.collect(Collectors.toList());

				throw new IllegalArgumentException(
						"Clusters with IDs " + alreadyAssignedClusterIds + " are already assigned to other calendars.");
			}

			// Create the mapping between the clusters and the calendar
			List<CalendarClusterMapping> clusterMappings = new ArrayList<>();
			clusterItems.forEach(clusterItem -> {
				CalendarClusterMapping mapping = new CalendarClusterMapping();
				mapping.setCalendar(calendar);
				mapping.setCluster(clusterItem);
				clusterMappings.add(mapping);
			});

			// Save all mappings
			calendarClusterRepo.addClusterToCalender(clusterMappings);
			log.info("Successfully added {} clusters to calendar {}", clusterMappings.size(), calendarKey);
		}, () -> {
			// Throw an error if the calendar is not found
			throw new ResourceNotFoundException("Calendar not found with key: " + calendarKey);
		});
	}

	@Override
	@AuditLog(action = "delete",entity = "CalendarClusterMapping",functionality = "delete Clusters from Calender")
	public void removeClustersFromCalendar(String calendarKey, List<Long> clusterIds) {
		log.info("Request received for removing clusters from calendar: {}", calendarKey);

		// Fetch the calendar by its key
		Optional<Calendar> calendarOpt = calendarRepository.findByCalendarKey(calendarKey);
		calendarOpt.ifPresentOrElse(calendar -> {
			// Fetch existing mappings for the calendar and clusters
			List<CalendarClusterMapping> mappings = calendarClusterRepo.findByCalendarAndClusterIdIn(calendar,
					clusterIds);

			if (mappings.isEmpty()) {
				throw new IllegalArgumentException("No cluster mappings found for the given cluster IDs and calendar");
			}

			// Delete the mappings
			calendarClusterRepo.deleteAll(mappings);

			log.info("Successfully removed {} clusters from calendar: {}", mappings.size(), calendarKey);
		}, () -> {
			log.error("Calendar not found with key: {}", calendarKey);
			throw new ResourceNotFoundException("Calendar not found with key: " + calendarKey);
		});
	}

	@Override
	public void updateClustersForCalendar(String oldCalendarKey, String newCalendarKey,
			ClustersToCalendarDto clusters) {
		log.info("Request received for updating clusters from calendar {} to calendar {}", oldCalendarKey,
				newCalendarKey);

		// Fetch the old calendar by its key
		Optional<Calendar> oldCalendarOpt = calendarRepository.findByCalendarKey(oldCalendarKey);
		oldCalendarOpt.ifPresentOrElse(oldCalendar -> {
			calendarRepository.save(oldCalendar);
			// Fetch the new calendar by its key
			Optional<Calendar> newCalendarOpt = calendarRepository.findByCalendarKey(newCalendarKey);
			newCalendarOpt.ifPresentOrElse(newCalendar -> {
				// Get the list of clusters from the provided cluster IDs
				List<ClusterItem> clusterItems = clusterRepo.findAllById(clusters.getClusterIds());

				// Check if all provided clusters exist in the repository
				if (clusterItems.size() != clusters.getClusterIds().size()) {
					throw new IllegalArgumentException("Some of the clusters were not found");
				}

				// Check if any of the clusters are already assigned to another calendar
				List<CalendarClusterMapping> existingMappings = calendarClusterRepo.findByClusterIn(clusterItems);
				if (!existingMappings.isEmpty()) {
					// Filter out clusters that are already assigned to another calendar
					List<ClusterItem> alreadyAssignedClusters = existingMappings.stream()
							.map(CalendarClusterMapping::getCluster).collect(Collectors.toList());

					List<Long> alreadyAssignedClusterIds = alreadyAssignedClusters.stream().map(ClusterItem::getId)
							.collect(Collectors.toList());

					// Check if these clusters are assigned to the current calendar
					List<ClusterItem> clustersToRemoveFromOldCalendar = existingMappings.stream()
							.filter(mapping -> mapping.getCalendar().equals(oldCalendar))
							.map(CalendarClusterMapping::getCluster).collect(Collectors.toList());

					// Remove any mappings from the old calendar for the clusters to be updated
					List<CalendarClusterMapping> mappingsToRemove = calendarClusterRepo
							.findByCalendarAndClusterIdIn(oldCalendar, clustersToRemoveFromOldCalendar.stream()
									.map(ClusterItem::getId).collect(Collectors.toList()));

					if (!mappingsToRemove.isEmpty()) {
						calendarClusterRepo.deleteAll(mappingsToRemove);
						log.info("Successfully removed {} clusters from old calendar: {}", mappingsToRemove.size(),
								oldCalendarKey);
					}

					// If these clusters are already assigned to another calendar (not the same
					// one), throw an error
					if (alreadyAssignedClusters.size() > 0) {
						List<Long> alreadyAssignedClusterIdsNotInOldCalendar = alreadyAssignedClusterIds.stream()
								.filter(id -> !clustersToRemoveFromOldCalendar.stream()
										.anyMatch(c -> c.getId().equals(id)))
								.collect(Collectors.toList());
						if (!alreadyAssignedClusterIdsNotInOldCalendar.isEmpty()) {
							throw new IllegalArgumentException(
									"Clusters with IDs " + alreadyAssignedClusterIdsNotInOldCalendar
											+ " are already assigned to other calendars.");
						}
					}
				}

				// Create new mappings for clusters to the new calendar
				List<CalendarClusterMapping> clusterMappings = new ArrayList<>();
				clusterItems.forEach(clusterItem -> {
					CalendarClusterMapping mapping = new CalendarClusterMapping();
					mapping.setCalendar(newCalendar);
					mapping.setCluster(clusterItem);
					clusterMappings.add(mapping);
				});

				// Save the new mappings to the new calendar
				calendarClusterRepo.saveAll(clusterMappings);
				log.info("Successfully updated and added {} clusters to new calendar {}", clusterMappings.size(),
						newCalendarKey);

			}, () -> {
				log.error("New calendar not found with key: {}", newCalendarKey);
				throw new ResourceNotFoundException("New calendar not found with key: " + newCalendarKey);
			});

		}, () -> {
			log.error("Old calendar not found with key: {}", oldCalendarKey);
			throw new ResourceNotFoundException("Old calendar not found with key: " + oldCalendarKey);
		});
	}

	@Override
	public List<ClusterDto> fetchClustersByCalendar(String calendarKey) {
		log.info("Fetching clusters for calendar {}", calendarKey);

		// List to store the ClusterDto objects
		List<ClusterDto> dtos = new ArrayList<>();

		// Find calendar by the calendarKey
		Optional<Calendar> record = calendarRepository.findByCalendarKey(calendarKey);

		// If the calendar is found, process its associated clusters
		record.ifPresentOrElse(value -> {
			// Fetch associated CalendarClusterMapping entities
			Set<CalendarClusterMapping> calendarClusterMappings = value.getClusterCalendar();

			// Iterate through each CalendarClusterMapping to extract the clusters
			calendarClusterMappings.forEach(mapping -> {
				ClusterItem cluster = mapping.getCluster(); // Access the ClusterItem

				// Only include clusters that are active
				if (cluster.getActive() != 0) { // Active cluster check
					// Map ClusterItem to ClusterDto using ModelMapper
					ClusterDto dto = mapper.map(cluster, ClusterDto.class);
					dto.setName(cluster.getName());
					dto.setRemark(cluster.getRemark());
					dtos.add(dto); // Add the ClusterDto to the list
				}
			});

		}, () -> {
			log.info("Calendar record not found");
			throw new ResourceNotFoundException(String.format("Calendar with Id %s not found", calendarKey));
		});

		return dtos; // Return the list of clusters as DTOs
	}

	@Override
	public List<ClusterDto> getClustersNotAssignedToAnyCalendar() {
		log.info("Fetching clusters that are not assigned to any calendar");

		// Get all clusters
		List<ClusterItem> allClusters = clusterRepo.findAll();

		// Find clusters that are not assigned to any calendar by checking if they are
		// in any mapping
		List<Long> assignedClusterIds = calendarClusterRepo.findAll().stream()
				.map(mapping -> mapping.getCluster().getId()).collect(Collectors.toList());

		// Filter out the clusters that are assigned to a calendar and also check if
		// they are active
		List<ClusterDto> unassignedClusters = allClusters.stream()
				.filter(cluster -> !assignedClusterIds.contains(cluster.getId())) // Check if not assigned
				.filter(cluster -> cluster.getActive() != 0) // Check if active (active != 0)
				.map(cluster -> {
					// Map ClusterItem to ClusterDto using ModelMapper
					ClusterDto dto = mapper.map(cluster, ClusterDto.class);
					// Additional fields from ClusterItem to ClusterDto
					dto.setName(cluster.getName());
					dto.setRemark(cluster.getRemark());
					return dto;
				}).collect(Collectors.toList());

		log.info("Found {} active clusters not assigned to any calendar", unassignedClusters.size());

		return unassignedClusters;
	}

	@Override
	public CalendarDetailsDto getCalendarDetails(Integer clusterId, LocalDate date) {
		// 1. Fetch calendar for the given clusterId
		Optional<CalendarClusterMapping> mappingData = calendarClusterRepo.findByCluster_Id(clusterId);
				
		if(mappingData.isEmpty()) {
			return null;
		}
			
		CalendarClusterMapping mapping = mappingData.get();
		Calendar calendar = mapping.getCalendar();
		String calendarKey = calendar.getCalendarKey();
		
		if (calendar.getYearTo() < date.getYear()) {
			throw new InvalidCalendarException(String.format("Calendar assigned to this cluster %d is expired as calendar available year is already passed", clusterId));
		}

		// 2. Get work start and end time from the Calendar entity
		LocalTime workStart = calendar.getWorkTimeStart();
		LocalTime workEnd = calendar.getWorkTimeEnd();

		// 3. Fetch workweek details
		List<CalendarWorkWeek> workWeekList = workWeekRepo.findByCalendar_CalendarKey(calendarKey);
		List<String> workingDays = workWeekList.stream().map(week -> week.getWeekdayKey().name())
				.collect(Collectors.toList());

		// 4. Fetch holidays in the range (date - 2 days) to (date + 15 days)
		LocalDate startDate = date.minusDays(2);
		LocalDate endDate = date.plusDays(15);

		List<CalendarHolidays> holidays = holidaysRepo.findByCalendar_CalendarKeyAndHolidayDateBetween(calendarKey,startDate, endDate);
		List<CalendarHolidaysDto> holidayDtos = holidays.stream().map(h -> new CalendarHolidaysDto(h.getIdHoliday(), // Ensure
				h.getCalendar().getCalendarKey(), h.getHolidayName(), h.getValidEachYear(), h.getHolidayDate(),
				h.getRemark())).collect(Collectors.toList());

		// Prepare response
		return new CalendarDetailsDto(clusterId, calendarKey, workingDays, workStart, workEnd, holidayDtos);
	}

}
