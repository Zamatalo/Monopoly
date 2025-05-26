package com.example.application.service;

import com.example.application.redis.RedisStreamResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class GameGatewayService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisStreamResponseHandler responseHandler;

    public CompletableFuture<String> getAllGames() {
        String correlationId = UUID.randomUUID().toString();

        Map<String, String> body = new HashMap<>();
        body.put("correlationId", correlationId);
        body.put("action", "getAllGames");

        redisTemplate.opsForStream().add("game.request", body);

        return responseHandler.register(correlationId);
    }

    public CompletableFuture<String> getGame_PlayerId(String playerId) {
        String correlationId = UUID.randomUUID().toString();

        Map<String, String> body = new HashMap<>();
        body.put("correlationId", correlationId);
        body.put("action", "findGameByPlayerId");
        body.put("playerId", playerId);
        redisTemplate.opsForStream().add("game.request", body);

        return responseHandler.register(correlationId);
    }
}
