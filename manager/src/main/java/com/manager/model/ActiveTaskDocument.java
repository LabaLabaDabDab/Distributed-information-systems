package com.manager.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.manager.enums.TaskStatus;
import lombok.Data;

import java.util.List;

@Data
public class ActiveTaskDocument {
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

    @JsonProperty("status")
    private TaskStatus status;
}
