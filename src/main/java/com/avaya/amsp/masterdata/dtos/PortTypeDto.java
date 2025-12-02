package com.avaya.amsp.masterdata.dtos;

import java.sql.Timestamp;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PortTypeDto {
	
	Long id;
	
	//@NotNull
	//@Size(min = 1,max = 45,message = "type should not be blank")
	String type;
	
	String name;
	String description;
	
	//@NotNull
	//@Min(value = 0,message = "pseudoFlag should have value either 0 or 1")
	//@Max(value = 1,message = "pseudoFlag should have value either 0 or 1")
	int pseudoFlag;
	
	int hidden;
	
	//@NotNull
	//@Size(min = 1,max = 255,message = "user should not be blank")
	String user;
	
	Timestamp createdOn;

}
