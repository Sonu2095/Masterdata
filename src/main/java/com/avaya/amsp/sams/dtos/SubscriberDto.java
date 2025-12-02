package com.avaya.amsp.sams.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubscriberDto {
    private String uniqueIdentifier;
    private String source;
    private String userPrincipalName;
    private Long amfkTlnid;
    private String sn;
    private String givenName;
    private String displayName;
    private String department;
    private String telephoneNumber;
    private String telephoneNumberNorm;
    private String telephonePrefix;
    private String telephoneExtension;
    private String mobile;
    private String facsimileTelephoneNumber;
    private String mail;
    private String proxyAddresses;
    private String associatedDomain;
    private String samAccountName;
    private String company;
    private String street;
    private String postalCode;
    private String location;
    private String roomNumber;
    private String country;
    private String costCenter;
    private String pager;
    private String faxCoverSheet;
    private String imHandler;
    private String skypeTelephoneNumber;
    private String msRtcSipLine;
    private String msRtcSipAddress;
    private Long personStatus;
    private Long accountStatus;
    private String accountType;
    private String katKey;
    private Long pinNotExpire;
    private String smLoginName;
    private String description;
    private String syncLock;
    private String matchState;
    private String matchType;
    private LocalDateTime whenChanged;
    private LocalDateTime whenInserted;
    private String changedBy;
    private String insertedBy;
    private Long isDeleted;
    private String oLocationCode;
    private String aLocationCode;
    private Long oLocationId;
    private Long aLocationId;
    private Long viewRowId;
    private LocalDateTime lastSyncTimestamp;
}
