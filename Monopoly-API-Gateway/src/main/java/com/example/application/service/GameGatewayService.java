package com.example.application.service;

import com.example.application.redis.RedisStreamResponseHandler;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class GameGatewayService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisStreamResponseHandler responseHandler;
    private final ObjectMapper objectMapper;

    public <T> CompletableFuture<T> sendAction(String action, Class<T> clazz) {
        return sendAction(action, new HashMap<>(), clazz);
    }

    public <T> CompletableFuture<T> sendAction(String action, Map<String, String> args, Class<T> clazz) {
        String correlationId = UUID.randomUUID().toString();
        Map<String, String> body = new HashMap<>(args);
        body.put("correlationId", correlationId);
        body.put("action", action);
        StringRecord record = StreamRecords.string(body).withStreamKey("game.request");

        var javaType = objectMapper.getTypeFactory().constructType(clazz);

        redisTemplate.opsForStream().add(record);

        return responseHandler.register(correlationId, javaType);
    }

}
