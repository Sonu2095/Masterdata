package com.avaya.amsp.masterdata.dtos;

import java.time.LocalDateTime;

import com.avaya.amsp.domain.PortStatusEnum;

import lombok.Data;

@Data
public class PbxPortDto {
	private Long id;
	private Long idPbxSystem;
	private Long idPbxCluster;
	private Long idCluster;
	private Long idPortType;
	private Long idPortTypeAem;
	private String pbxPanel;
	private String pbxPanelPort;
	private String netPanel;
	private String netPanelPort;
	private String portName;
	private String remark;
	private PortStatusEnum status;
	private String logCreatedBy;
	private LocalDateTime logCreatedOn;
	private String logUpdatedBy;
	private LocalDateTime logUpdatedOn;

}
