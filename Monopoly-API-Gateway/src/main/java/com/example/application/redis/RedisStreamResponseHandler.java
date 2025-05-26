package com.example.application.redis;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RedisStreamResponseHandler {

    private final Map<String, CompletableFuture<String>> futures = new ConcurrentHashMap<>();

    public CompletableFuture<String> register(String correlationId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        futures.put(correlationId, future);
        return future.orTimeout(5, TimeUnit.SECONDS);
    }

    public void complete(String correlationId, String response) {
        CompletableFuture<String> future = futures.remove(correlationId);
        if (future != null) {
            future.complete(response);
        }
    }
}
