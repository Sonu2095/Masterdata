package com.avaya.amsp.masterdata.service;

import com.avaya.amsp.domain.Subscribers;
import com.avaya.amsp.masterdata.dtos.GetExtensionResponseDto;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.SubscriberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.sql.Timestamp;

@Service
@Slf4j
public class ReserveExtensionService {

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Transactional
    public GetExtensionResponseDto reserveExtension(Long areaCode, String extension, Integer maxReservationTimeInSecs) {
        log.info("Reserving extension for areaCode: {}, extension: {}, maxReservationTimeInSecs: {}", 
                areaCode, extension, maxReservationTimeInSecs);
        
        // Convert areaCode from Long to String for repository query
        String areaCodeStr = String.valueOf(areaCode);
        
        // Find the subscriber
        Subscribers subscriber = subscriberRepository.findByAreaCodeAndExtension(areaCodeStr, extension)
                .orElseThrow(() -> {
                    log.warn("Subscriber not found for areaCode: {} and extension: {}", areaCode, extension);
                    throw new ResourceNotFoundException("Extension was not found for the given area code");
                });

        // Check if extension is in "free" state
        String currentState = subscriber.getCurrentState();
        if (currentState != null && !currentState.trim().isEmpty() && !currentState.equalsIgnoreCase("free")) {
            // Extension is not free
            if (currentState.equalsIgnoreCase("reserved")) {
                log.warn("Extension is already reserved for areaCode: {} and extension: {}", areaCode, extension);
                throw new IllegalStateException("EXTENSION_ALREADY_RESERVED");
            } else {
                log.warn("Extension is not free (current state: {}) for areaCode: {} and extension: {}", 
                        currentState, areaCode, extension);
                throw new IllegalStateException("EXTENSION_NOT_FREE");
            }
        }

        // Reserve the extension
        subscriber.setCurrentState("reserved");
        subscriber.setFromUser("ORDERBRIDGE");
        
        // Calculate reserved until date
        Instant reservedUntilDate;
        if (maxReservationTimeInSecs != null && maxReservationTimeInSecs > 0) {
            reservedUntilDate = Instant.now().plusSeconds(maxReservationTimeInSecs);
        } else {
            // No limit - set to a far future date (e.g., 10 years from now)
            reservedUntilDate = Instant.now().plusSeconds(10L * 365 * 24 * 60 * 60);
        }
        
        // Set lastVerificationAt to the reserved until date
        subscriber.setLastVerificationAt(Timestamp.from(reservedUntilDate));
        
        // Save the updated subscriber
        subscriberRepository.save(subscriber);
        
        log.info("Successfully reserved extension for areaCode: {} and extension: {}", areaCode, extension);
        
        // Map to response DTO
        return mapToGetExtensionResponse(subscriber, areaCode, reservedUntilDate);
    }

    private GetExtensionResponseDto mapToGetExtensionResponse(Subscribers subscriber, Long areaCode, Instant reservedUntilDate) {
        GetExtensionResponseDto response = new GetExtensionResponseDto();

        // Map callNumber
        GetExtensionResponseDto.CallNumber callNumber = new GetExtensionResponseDto.CallNumber();
        callNumber.setAreaCode(areaCode);
        callNumber.setExtension(subscriber.getExtension());
        response.setCallNumber(callNumber);

        // Map connectionType
        GetExtensionResponseDto.ConnectionType connectionType = new GetExtensionResponseDto.ConnectionType();
        String bcsType = determineBCSType(subscriber.getConnectionType(), subscriber.getBcsBunch());
        connectionType.setBCSType(bcsType);
        response.setConnectionType(connectionType);

        // Map status
        GetExtensionResponseDto.Status status = new GetExtensionResponseDto.Status();
        status.setStatus("reserved");
        
        GetExtensionResponseDto.StatusInfo statusInfo = new GetExtensionResponseDto.StatusInfo();
        statusInfo.setReservedBy(subscriber.getFromUser() != null ? subscriber.getFromUser() : "ORDERBRIDGE");
        statusInfo.setReservedUntilDate(reservedUntilDate);
        status.setInfo(statusInfo);
        response.setStatus(status);

        // Map user (may be empty when status is reserved according to docs)
        GetExtensionResponseDto.User user = new GetExtensionResponseDto.User();
        user.setDomain(subscriber.getNtDomain() != null ? subscriber.getNtDomain() : "");
        user.setUserID(subscriber.getNtUsername() != null ? subscriber.getNtUsername() : "");
        response.setUser(user);

        return response;
    }

    private String determineBCSType(String connectionType, String bcsBunch) {
        if (connectionType != null) {
            if (connectionType.toUpperCase().contains("SFB")) {
                return "BCS SFB";
            } else if (connectionType.toUpperCase().contains("TEAMS")) {
                return "BCS Teams";
            }
            return connectionType;
        }
        if (bcsBunch != null && !bcsBunch.isEmpty()) {
            return bcsBunch;
        }
        return "BCS SFB";
    }
}

