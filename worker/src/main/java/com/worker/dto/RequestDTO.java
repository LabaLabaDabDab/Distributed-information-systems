package com.worker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RequestDTO {
    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("maxLength")
    private int maxLength;

    @JsonProperty("alphabet")
    private List<String> alphabet;
}