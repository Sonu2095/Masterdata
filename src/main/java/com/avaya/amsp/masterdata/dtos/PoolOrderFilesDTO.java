/**
 * 
 */
package com.avaya.amsp.masterdata.dtos;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 
 */
@Data
public class PoolOrderFilesDTO {
	
    private String fileId;
    private String fileName;
    private Integer sizeInBytes;
    private LocalDateTime uploadTs;

}
