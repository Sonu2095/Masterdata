package com.avaya.amsp.masterdata.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ArticleToClusterSyncDto {

    @NotNull
    @Size(min = 1)
    List<Long> clusterIds;

    @NotNull
    @Size(min = 1)
    List<Integer> attributeIds;
}
