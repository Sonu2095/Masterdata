package com.avaya.amsp.masterdata.dtos;

import java.time.LocalDateTime;

import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.PbxSystem;
import com.avaya.amsp.domain.PortType;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class PbxSpecialPortDto {
    private Long id;
    private String hwa;
    private String remark;
    private String logCreatedBy;
    private LocalDateTime logCreatedOn;
    private String logUpdatedBy;
    private LocalDateTime logUpdatedOn;
    private Long idCluster;
    private Long idPbxSystem;
    private Long idPortType;
    private Long idPortTypeAem;
}
