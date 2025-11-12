package com.avaya.amsp.masterdata.service;

import com.avaya.amsp.domain.Subscribers;
import com.avaya.amsp.masterdata.dtos.GetExtensionResponseDto;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.SubscriberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class GetExtensionService {

    @Autowired
    private SubscriberRepository subscriberRepository;

    public GetExtensionResponseDto getExtensionByAreaCodeAndExtension(Long areaCode, String extension) {
        log.info("Fetching subscriber for areaCode: {} and extension: {}", areaCode, extension);
        
        // Convert areaCode from Long to String for repository query
        String areaCodeStr = String.valueOf(areaCode);
        
        Subscribers subscriber = subscriberRepository.findByAreaCodeAndExtension(areaCodeStr, extension)
                .orElseThrow(() -> {
                    log.warn("Subscriber not found for areaCode: {} and extension: {}", areaCode, extension);
                    throw new ResourceNotFoundException("Extension was not found for the given area code");
                });

        return mapToGetExtensionResponse(subscriber, areaCode);
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
        // Map connectionType to BCSType - if connectionType contains "SFB", use "BCS SFB", otherwise use connectionType or bcsBunch
        String bcsType = determineBCSType(subscriber.getConnectionType(), subscriber.getBcsBunch());
        connectionType.setBCSType(bcsType);
        response.setConnectionType(connectionType);

        // Map status
        GetExtensionResponseDto.Status status = new GetExtensionResponseDto.Status();
        // Use currentState as status, default to "active" if null
        status.setStatus(subscriber.getCurrentState() != null ? subscriber.getCurrentState() : "active");
        
        GetExtensionResponseDto.StatusInfo statusInfo = new GetExtensionResponseDto.StatusInfo();
        // Map fromUser to reservedBy if available
        statusInfo.setReservedBy(subscriber.getFromUser() != null ? subscriber.getFromUser() : "ORDERBRIDGE");
        // Use lastVerificationAt or createdAt as reservedUntilDate, or set a default future date
        if (subscriber.getLastVerificationAt() != null) {
            statusInfo.setReservedUntilDate(subscriber.getLastVerificationAt().toInstant());
        } else if (subscriber.getCreatedAt() != null) {
            statusInfo.setReservedUntilDate(subscriber.getCreatedAt().toInstant());
        } else {
            // Default to 30 days from now if no date available
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
            // Return connectionType as is if it doesn't match known patterns
            return connectionType;
        }
        // Fallback to bcsBunch if connectionType is null
        if (bcsBunch != null && !bcsBunch.isEmpty()) {
            return bcsBunch;
        }
        // Default fallback
        return "BCS SFB";
    }
}

