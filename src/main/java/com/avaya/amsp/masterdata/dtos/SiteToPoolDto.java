package com.avaya.amsp.masterdata.dtos;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SiteToPoolDto {

	@NotNull
	@Size(min = 1)
	List<Long> poolIds;

	//@NotNull
	//@Size(min = 1, max = 255, message = "user should not be blank")
	String user;

}
