package com.avaya.amsp.masterdata.dtos;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderPartListDto {

	private ArticlePropertiesDto article;

	private Set<ArticlePropertiesDto> insert = new HashSet<>();
	private Set<ArticlePropertiesDto> delete = new HashSet<>();
	private Set<ArticlePropertiesDto> change = new HashSet<>();
	private Set<ArticlePropertiesDto> none = new HashSet<>();

}
