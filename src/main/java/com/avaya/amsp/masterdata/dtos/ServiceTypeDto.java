package com.avaya.amsp.masterdata.dtos;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 
 * @author mreddys 
 * This class is working as a DTO for ServiceType
 *
 */

@Data
public class ServiceTypeDto {

    private long id;
    private String name;
    private String description;
	private String logCreatedBy;
	private String logUpdatedBy;

}
