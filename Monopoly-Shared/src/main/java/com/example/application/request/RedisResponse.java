package com.example.application.request;

import java.io.Serializable;
import java.util.Map;

public record RedisResponse(Map<String,Object> body, String correlationId) implements Serializable {
}