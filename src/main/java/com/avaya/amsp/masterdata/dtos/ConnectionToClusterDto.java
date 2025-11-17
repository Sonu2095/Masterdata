package com.avaya.amsp.masterdata.dtos;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConnectionToClusterDto {

	@NotNull
	@Size(min = 1)
	List<Long> connectionIds;

	String user;

}
