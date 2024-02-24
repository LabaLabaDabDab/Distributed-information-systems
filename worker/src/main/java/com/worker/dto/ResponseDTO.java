package com.worker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ResponseDTO {
    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("answers")
    private List<String> answers;
}