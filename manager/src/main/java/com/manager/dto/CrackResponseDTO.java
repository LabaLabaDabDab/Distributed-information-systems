package com.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CrackResponseDTO {
    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("partNumber")
    private Integer partNumber;

    @JsonProperty("answers")
    private List<String> answers;

}
