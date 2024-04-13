package com.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.manager.enums.TaskStatus;
import lombok.Data;

import java.util.List;

@Data
public class StatusResponseDTO {
    @JsonProperty("status")
    private TaskStatus status;

    @JsonProperty("data")
    private List<String> data;

    public StatusResponseDTO(TaskStatus status, List<String> data) {
        this.status = status;
        this.data = data;
    }
}
