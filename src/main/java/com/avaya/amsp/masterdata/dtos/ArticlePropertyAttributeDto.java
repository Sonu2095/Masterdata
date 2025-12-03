package com.avaya.amsp.masterdata.dtos;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticlePropertyAttributeDto {

	@NotNull
	private Long articleId; // represent article id

	private String name;
	private String remark;
	private String nameEnglish;
	private String nameGerman;

	@Min(value = 0, message = "pseudoFlag should have value either 0 or 1")
	@Max(value = 1, message = "pseudoFlag should have value either 0 or 1")
	private Boolean obligatory;

	@Min(value = 0, message = "pseudoFlag should have value either 0 or 1")
	@Max(value = 1, message = "pseudoFlag should have value either 0 or 1")
	private Boolean alwaysInsert;

	@Min(value = 0, message = "pseudoFlag should have value either 0 or 1")
	@Max(value = 1, message = "pseudoFlag should have value either 0 or 1")
	private Boolean alwaysMove;

	@Min(value = 0, message = "pseudoFlag should have value either 0 or 1")
	@Max(value = 1, message = "pseudoFlag should have value either 0 or 1")
	private Boolean alwaysDelete;

	@Min(value = 0, message = "pseudoFlag should have value either 0 or 1")
	@Max(value = 1, message = "pseudoFlag should have value either 0 or 1")
	private Boolean alwaysChange;

	private Long quantity;
	private Long bcsId;

}
