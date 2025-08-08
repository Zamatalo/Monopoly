package com.example.application.redis;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Manages registration and completion of asynchronous requests identified by correlation IDs.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Game_StreamRequest {
    private final Map<String, FutureWrapper<?>> futures = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    /**
     * Registers a new future associated with the given correlation ID and expected response type.
     * <p>
     * This future will be completed when a response with the matching correlation ID is received.
     * </p>
     *
     * @param correlationId the unique correlation ID for the request/response pair
     * @param javaType      the Jackson {@link JavaType} representing the expected response type
     * @param <T>           the type of the expected response
     * @return a {@link CompletableFuture} that will be completed with the deserialized response object
     */
    public <T> CompletableFuture<T> register(String correlationId, JavaType javaType) {
        FutureWrapper<T> wrapper = new FutureWrapper<>(javaType);
        futures.put(correlationId, wrapper);
        return wrapper.future;
    }

    /**
     * Completes the registered future matching the given correlation ID with the provided JSON response.
     * <p>
     * If the response contains an error indication or the future does not exist, the future is cancelled or a warning is logged.
     * </p>
     *
     * @param correlationId the correlation ID identifying the future to complete
     * @param response      the JSON string response to deserialize and complete the future with
     */
    public void complete(String correlationId, String response) {
        FutureWrapper<?> wrapper = futures.remove(correlationId);

        if (wrapper == null) {
            log.warn("No future found for correlationId: {}", correlationId);
            return;
        }

        if (response==null ||
                response.contains("Invalid action") ||
                response.contains("Internal Server Error") ||
                response.contains("Player Not Found")) {
            wrapper.future.cancel(true);
            return;
        }

        try {
            var object = objectMapper.readValue(response,wrapper.javaType);
            CompletableFuture<Object> future = (CompletableFuture<Object>) wrapper.future;
            future.complete(object);
        } catch (Exception e) {
            wrapper.future.completeExceptionally(e);
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
