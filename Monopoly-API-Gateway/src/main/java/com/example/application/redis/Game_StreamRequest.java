package com.example.application.redis;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class Game_StreamRequest {
    private final Map<String, FutureWrapper<?>> futures = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;


    public <T> CompletableFuture<T> register(String correlationId, JavaType javaType) {
        FutureWrapper<T> wrapper = new FutureWrapper<>(javaType);
        futures.put(correlationId, wrapper);
        return wrapper.future;
    }

    public void complete(String correlationId, String response) {
        FutureWrapper<?> wrapper = futures.remove(correlationId);
        if (response.contains("Invalid action")) {
            wrapper.future.cancel(true);
        }

        if (wrapper != null) {
            try {
                var json = objectMapper.readTree(response).asText();
                var object =objectMapper.readValue(json,wrapper.javaType);
                CompletableFuture<Object> future = (CompletableFuture<Object>) wrapper.future;
                future.complete(object);
            } catch (Exception e) {
                wrapper.future.completeExceptionally(e);
            }
        }
    }

    private static class FutureWrapper<T> {
        final JavaType javaType;
        final CompletableFuture<T> future;

        FutureWrapper(JavaType javaType) {
            this.javaType = javaType;
            this.future = new CompletableFuture<>();
        }
    }
}
