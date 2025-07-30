package com.example.application.utility;

import com.example.application.services.reactive.RedisService_Mono;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
public record RequestContextRedis(
        String correlationId,
        Map<String, String> body,
        RedisService_Mono redisService,
        ObjectMapper objectMapper
) {

    public Mono<Void> respond(Object responseData) {
        return buildResponseRecord(responseData)
                .flatMap(redisService::publishToResponseStream)
                .then();
    }

    private Mono<Map<String, String>> buildResponseRecord(Object responseData) {
        return Mono.fromCallable(() -> {
            String payloadJson = responseData == null
                    ? ""
                    : objectMapper.writeValueAsString(responseData);
            return Map.of(
                    "correlationId", correlationId,
                    "payload", payloadJson
            );
        });
    }
}
