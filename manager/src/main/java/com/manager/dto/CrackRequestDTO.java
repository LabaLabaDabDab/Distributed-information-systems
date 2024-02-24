package com.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CrackRequestDTO {
    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("maxLength")
    private int maxLength;

    @JsonProperty("alphabet")
    private List<String> alphabet;
}
