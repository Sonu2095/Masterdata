package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class PbxSystemDto {
    private Long id;
    private String physicalPbx;
    private String aemPbx;
    private boolean sfbsSystem;
    private boolean teamsSystem;
    private String flCode;
    private String shippingEmailId;
    private String assemblingEmailId;
    private String sipDomain;
    private String remark;
    private String routingPolicyName;
    private String cmName;
    private String notes;
    private boolean arsAnalysisEntry;
    private String logCreatedBy;
    private Timestamp logCreatedOn;
    private String logUpdatedBy;
    private Timestamp logUpdatedOn;
    private String userStamp;
	private Timestamp timeStamp;

    List<Long> pbxSytemSiteIds = new ArrayList<Long>();

    private PbxClusterDto pbxCluster;
    
    private List<SiteDto> sites;

}
