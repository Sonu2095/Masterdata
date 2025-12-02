package com.avaya.amsp.masterdata.dtos;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class CalendarDayLightSavingDto {
    private Long idDst;
    private String calendarKey;
    private Integer dstYear;  // Added dstYear to match the entity

    private Timestamp switchToSummerTime;
    private Timestamp returnToStandardTime;

    private Double hourOffset;
    private String remark;
}
