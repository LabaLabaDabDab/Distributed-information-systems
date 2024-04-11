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
    private Integer maxLength;

    @JsonProperty("alphabet")
    private List<String> alphabet;

    @JsonProperty("partNumber")
    private Integer partNumber;

    @JsonProperty("partAlphabet")
    private List<String> partAlphabet;
}
