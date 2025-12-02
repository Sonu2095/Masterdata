package com.avaya.amsp.masterdata.dtos;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CalendarWorkWeekDto {
    @JsonIgnore	
    private Long idWorkweek;
    private String calendarKey;
    private List<String> weekdayKeys;  // List of weekdays
    private String remark;
    private String weekdayKey;
    
    @JsonIgnore
    private LocalTime workTimeStart = LocalTime.of(9, 0, 0); // Default value

    @JsonIgnore
    private LocalTime workTimeEnd = LocalTime.of(19, 0, 0); // Default value

    // Custom Getter for Serialization
    @JsonProperty("workTimeStart")
    @Schema(description = "Start time of the work day in HH:mm:ss format", example = "09:00:00", defaultValue = "09:00:00")
    public String getWorkTimeStartFormatted() {
        return workTimeStart != null ? workTimeStart.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "09:00:00";
    }

    @JsonProperty("workTimeEnd")
    @Schema(description = "End time of the work day in HH:mm:ss format", example = "19:00:00", defaultValue = "19:00:00")
    public String getWorkTimeEndFormatted() {
        return workTimeEnd != null ? workTimeEnd.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "19:00:00";
    }

    // Custom Setter for Deserialization
    @JsonProperty("workTimeStart")
    public void setWorkTimeStartFromString(String time) {
        this.workTimeStart = time != null ? LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm:ss")) : LocalTime.of(9, 0, 0);
    }

    @JsonProperty("workTimeEnd")
    public void setWorkTimeEndFromString(String time) {
        this.workTimeEnd = time != null ? LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm:ss")) : LocalTime.of(19, 0, 0);
    }
}
