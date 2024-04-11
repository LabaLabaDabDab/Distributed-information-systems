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
    private Integer maxLength;

    @JsonProperty("alphabet")
    private List<String> alphabet;

    @JsonProperty("partAlphabet")
    private List<String> partAlphabet;

    @JsonProperty("partNumber")
    private Integer partNumber;
}
