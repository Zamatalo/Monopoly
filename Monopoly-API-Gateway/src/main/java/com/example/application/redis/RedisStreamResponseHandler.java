package com.example.application.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RedisStreamResponseHandler {

    private final Map<String, CompletableFuture<String>> futures = new ConcurrentHashMap<>();

    @Value("${redis.response.timeout.seconds:5}")
    private int responseTimeoutSeconds;

    public CompletableFuture<String> register(String correlationId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        futures.put(correlationId, future);
        return future.orTimeout(responseTimeoutSeconds, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    futures.remove(correlationId);
                    return "{\"error\":\"Request timed out\"}";
                });
    }

    public void complete(String correlationId, String response) {
        CompletableFuture<String> future = futures.remove(correlationId);
        if (future != null) {
            future.complete(response);
        }
    }
}
