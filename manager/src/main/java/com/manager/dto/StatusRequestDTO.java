package com.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StatusRequestDTO {
    @JsonProperty("requestId")
    private String requestId;
}
