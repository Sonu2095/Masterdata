package com.avaya.amsp.sams.dtos;

import lombok.Data;

@Data
public class PropertyDto {

    private ArticlePropertiesDto insert;
    private ArticlePropertiesDto delete;
    private ArticlePropertiesDto change;
    private ArticlePropertiesDto none;

}
