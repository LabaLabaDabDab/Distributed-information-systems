package com.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StatusWorkDTO {
    @JsonProperty("statusWork")
    private String statusWork;
}
