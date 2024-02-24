package com.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HashDTO {
    @JsonProperty("hash")
    private String hash;

    @JsonProperty("maxLength")
    private int maxLength;
}
