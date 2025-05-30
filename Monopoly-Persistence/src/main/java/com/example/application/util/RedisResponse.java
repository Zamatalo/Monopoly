package com.example.application.util;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

public record RedisResponse(
        @JsonProperty("body") Map<String, Object> body,
        @JsonProperty("correlationId") String correlationId
) implements Serializable {

}
