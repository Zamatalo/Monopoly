package com.example.application.service;

import com.example.application.redis.RedisStreamResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
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

    public CompletableFuture<String> sendAction(String action) {
        return sendAction(action, new HashMap<>());
    }

    public CompletableFuture<String> sendAction(String action, String argName, String argValue) {
        Map<String, String> additionalArgs = new HashMap<>();
        additionalArgs.put(argName, argValue);
        return sendAction(action, additionalArgs);
    }

    public CompletableFuture<String> sendAction(String action, Map<String, String> additionalArgs) {
        String correlationId = UUID.randomUUID().toString();
        Map<String, String> body = new HashMap<>(additionalArgs);
        body.put("correlationId", correlationId);
        body.put("action", action);

        redisTemplate.opsForStream().add("game.request", body);
        return responseHandler.register(correlationId);
    }
}
