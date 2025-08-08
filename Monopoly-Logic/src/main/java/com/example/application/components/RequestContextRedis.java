package com.example.application.components;

import com.example.application.redis.RedisService_Mono;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.example.application.config.GameConfig.RESPONSE_STREAM;

/**
 * Context holder for a Redis request, encapsulating correlation ID,
 * request body, and services needed to send a response.
 * <p>
 * Provides functionality to send a response back to a Redis stream,
 * including JSON serialization of the response data.
 * </p>
 *
 * @param correlationId unique identifier to correlate request and response
 * @param body          map containing the request parameters
 * @param redisService  service for Redis interactions
 * @param objectMapper  Jackson ObjectMapper for JSON serialization
 */
@Slf4j
public record RequestContextRedis(
        String correlationId, Map<String, String> body,
        RedisService_Mono redisService,
        ObjectMapper objectMapper) {

    public Mono<Void> respond(Object responseData) {
        if ("true".equals(body.get("sentFromBot"))) {
            return Mono.empty();
        }

        return buildResponseRecord(responseData)
                .flatMap(e -> redisService.publishToStream(RESPONSE_STREAM, e))
                .then();
    }

    private Mono<Map<String, String>> buildResponseRecord(Object responseData) {
        return Mono.fromCallable(() -> {
            String payloadJson = responseData == null
                    ? ""
                    : objectMapper.writeValueAsString(responseData);
            String correlation = correlationId == null
                    ? ""
                    : correlationId;
            return Map.of(
                    "correlationId", correlation,
                    "payload", payloadJson
            );
        });
    }
}
