package com.avaya.amsp.masterdata.dtos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ArticlePropertiesReqDto {

	@Valid
	@NotNull
	@Size(min = 1)
	// private List<ArticlePropertiesDto> articleProperties;

	private List<ArticlePropertyAttributeDto> articleProperties;

}
