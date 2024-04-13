package com.manager.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.manager.enums.TaskStatus;
import lombok.Data;

import java.util.List;

@Data
public class ReadyTasksDocument {
    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("partNumber")
    private Integer partNumber;

    @JsonProperty("answers")
    private List<String> answers;

    @JsonProperty("status")
    private TaskStatus status;
}