package com.avaya.amsp.masterdata.dtos;

import lombok.Data;

@Data
public class UserDto {
	
	private String userAccount;
	private String firstName;
	private String lastName;
	private String email;
	private String fax;
	private String phoneNum;
	private String role;
	private String cluster;
	private String language;
	private String remark;
	private Integer passwordValidity;
	
}
