package com.avaya.amsp.masterdata.dtos;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SubscriberDto {
    private Long id;
    private Long idRegion;
    private Long idCluster;
    private Long idSite;
    private String idPbx;
    private String areaCode;
    private String extension;
    private String e164;
    private String name;
    private String firstName;
    private String ntUsername;
    private String ntDomain;
    private String costCenter;
    private String email;
    private String fax;
    private String mobile;
    private String pager;
    private String department;
    private String compasStatus;
    private String cicatStatus;
    private String accountType;
    private String roomOffice;
    private String typeOfUse;
    private String remark;
    private String connectionType;
    private String bcsBunch;
    private String msnMaster;
    private Boolean automaticSync;
    private String automaticDmtSync;
    private Boolean dataRecordBlocked;
    private Boolean amfkExpansion;
    private String fromUser;
    private Boolean whenPinAvailable;
    private String currentState;
    
    private String regionName;
    private String pbxName;
    private String clusterName;
    private String siteName;

//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Timestamp lastVerificationAt;

//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Timestamp lastSeenRegistered;

//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Timestamp createdAt;

 //   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Timestamp updatedAt;

    private String createdBy;
    private String updatedBy;
    private String userStamp;
	private Timestamp timeStamp;
	
	private String cicatOffice;
}
