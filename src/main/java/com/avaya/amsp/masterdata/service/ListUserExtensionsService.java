package com.avaya.amsp.masterdata.service;

import com.avaya.amsp.domain.Subscribers;
import com.avaya.amsp.masterdata.dtos.GetExtensionResponseDto;
import com.avaya.amsp.masterdata.dtos.ListUserExtensionsRequestDto;
import com.avaya.amsp.masterdata.repo.SubscriberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ListUserExtensionsService {

    @Autowired
    private SubscriberRepository subscriberRepository;

    public List<GetExtensionResponseDto> listUserExtensions(ListUserExtensionsRequestDto request) {
        log.info("Fetching extensions for domain: {}, userID: {}", request.getDomain(), request.getUserID());

        List<Subscribers> subscribers = subscriberRepository.findByDomainAndUser(
                request.getDomain(),
                request.getUserID()
        );

        return subscribers.stream()
                .map(this::mapToGetExtensionResponse)
                .collect(Collectors.toList());
    }

    private GetExtensionResponseDto mapToGetExtensionResponse(Subscribers subscriber) {
        GetExtensionResponseDto response = new GetExtensionResponseDto();

        GetExtensionResponseDto.CallNumber callNumber = new GetExtensionResponseDto.CallNumber();
        try {
            callNumber.setAreaCode(Long.parseLong(subscriber.getAreaCode()));
        } catch (Exception e) {
            callNumber.setAreaCode(0L);
        }
        callNumber.setExtension(subscriber.getExtension());
        response.setCallNumber(callNumber);

        GetExtensionResponseDto.ConnectionType connectionType = new GetExtensionResponseDto.ConnectionType();
        connectionType.setBCSType(determineBCSType(subscriber.getConnectionType(), subscriber.getBcsBunch()));
        response.setConnectionType(connectionType);

        GetExtensionResponseDto.Status status = new GetExtensionResponseDto.Status();
        status.setStatus(subscriber.getCurrentState() != null ? subscriber.getCurrentState() : "used");
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

    private Instant convertToInstant(Object timestamp) {
        if (timestamp == null) {
            return null;
        }
        if (timestamp instanceof Timestamp) {
            return ((Timestamp) timestamp).toInstant();
        } else if (timestamp instanceof java.time.LocalDateTime) {
            return ((java.time.LocalDateTime) timestamp).toInstant(ZoneOffset.UTC);
        } else {
            return Instant.now();
        }
    }
}

