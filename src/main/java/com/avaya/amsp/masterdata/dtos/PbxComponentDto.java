package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;
import lombok.Data;

@Data
public class PbxComponentDto {
    private Long id;
    private String hwa;
    private String remark;
    private String logCreatedBy;
    private Timestamp logCreatedOn;
    private String logUpdatedBy;
    private Timestamp logUpdatedOn;
    private Long idCluster;
    private Long idPbxSystem;

}
