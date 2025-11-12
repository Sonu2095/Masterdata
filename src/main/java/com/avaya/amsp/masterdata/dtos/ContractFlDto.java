package com.avaya.amsp.masterdata.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractFlDto {
    private Long id;
    private Long siteId;
    private String contractFL;
}
