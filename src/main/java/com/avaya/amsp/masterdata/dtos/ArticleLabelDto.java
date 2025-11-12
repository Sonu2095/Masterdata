package com.avaya.amsp.masterdata.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ArticleLabelDto {
	private String articleKey;
	private String articleNameGerman;
	private String articleNameEnglish;
	private String wizardHandler;
}
