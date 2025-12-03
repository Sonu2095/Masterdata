package com.avaya.amsp.masterdata.service;

import com.avaya.amsp.domain.Subscribers;
import com.avaya.amsp.masterdata.dtos.GetExtensionResponseDto;
import com.avaya.amsp.masterdata.dtos.ListExtensionRequestDto;
import com.avaya.amsp.masterdata.repo.SubscriberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ListExtensionService {

    @Autowired
    private SubscriberRepository subscriberRepository;

    public List<GetExtensionResponseDto> listExtensions(ListExtensionRequestDto request) {
        log.info("Fetching extensions for areaCode: {}, fromExtension: {}, toExtension: {}, status: {}, maxCount: {}, skipCount: {}", 
                request.getAreaCode(), request.getFromExtension(), request.getToExtension(), 
                request.getStatus() != null ? request.getStatus().getStatus() : null,
                request.getMaxCount(), request.getSkipCount());
        
        // Convert areaCode from Long to String for repository query
        String areaCodeStr = String.valueOf(request.getAreaCode());
        
        // Extract status if provided
        String statusFilter = null;
        if (request.getStatus() != null && request.getStatus().getStatus() != null) {
            statusFilter = request.getStatus().getStatus();
        }
        
        // Query subscribers by range
        List<Subscribers> subscribers = subscriberRepository.findByAreaCodeAndExtensionRange(
                areaCodeStr,
                request.getFromExtension(),
                request.getToExtension(),
                statusFilter
        );
        
        // Apply pagination (skipCount and maxCount)
        int skipCount = request.getSkipCount() != null && request.getSkipCount() > 0 ? request.getSkipCount() : 0;
        int maxCount = request.getMaxCount() != null && request.getMaxCount() > 0 ? request.getMaxCount() : Integer.MAX_VALUE;
        
        List<Subscribers> paginatedSubscribers = subscribers.stream()
                .skip(skipCount)
                .limit(maxCount)
                .collect(Collectors.toList());
        
        // Map to response DTOs
        return paginatedSubscribers.stream()
                .map(subscriber -> mapToGetExtensionResponse(subscriber, request.getAreaCode()))
                .collect(Collectors.toList());
    }

    private GetExtensionResponseDto mapToGetExtensionResponse(Subscribers subscriber, Long areaCode) {
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
        status.setStatus(subscriber.getCurrentState() != null ? subscriber.getCurrentState() : "active");
        
        GetExtensionResponseDto.StatusInfo statusInfo = new GetExtensionResponseDto.StatusInfo();
        statusInfo.setReservedBy(subscriber.getFromUser() != null ? subscriber.getFromUser() : "ORDERBRIDGE");
        if (subscriber.getLastVerificationAt() != null) {
            statusInfo.setReservedUntilDate(convertToInstant(subscriber.getLastVerificationAt()));
        } else if (subscriber.getCreatedAt() != null) {
            statusInfo.setReservedUntilDate(convertToInstant(subscriber.getCreatedAt()));
        } else {
            statusInfo.setReservedUntilDate(Instant.now().plusSeconds(30L * 24 * 60 * 60));
        }
        status.setInfo(statusInfo);
        response.setStatus(status);

        // Map user
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

    /**
     * Converts Timestamp or LocalDateTime to Instant
     */
    private Instant convertToInstant(Object timestamp) {
        if (timestamp == null) {
            return null;
        }
        
        if (timestamp instanceof Timestamp) {
            return ((Timestamp) timestamp).toInstant();
        } else if (timestamp instanceof java.time.LocalDateTime) {
            return ((java.time.LocalDateTime) timestamp).toInstant(ZoneOffset.UTC);
        } else {
            log.warn("Unexpected timestamp type: {}, using current time", timestamp.getClass().getName());
            return Instant.now();
        }
    }
}

