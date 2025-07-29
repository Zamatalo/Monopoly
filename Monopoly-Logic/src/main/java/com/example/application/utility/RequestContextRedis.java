package com.example.application.utility;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import reactor.core.publisher.Mono;
import util.exceptions.RedisResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisOperations;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public record RequestContextRedis(
        String correlationId,
        Map<String, String> body,
        MapRecord<String, String, String> rawRecord,
        ReactiveStreamOperations<String,String,String> operations,
        ObjectMapper objectMapper
) {

    public Mono<Void> respond(Object payload) {
        return Mono.fromCallable(() -> {
                    Map<String, String> body = new HashMap<>();
                    String payloadJson = objectMapper.writeValueAsString(payload);
                    body.put("payload", payloadJson);
                    body.put("correlationId", correlationId);
                    return StreamRecords.newRecord()
                            .ofMap(body)
                            .withStreamKey("game.response");
                })
                .flatMap(operations::add)
                .doOnSuccess(r -> log.debug("Sent response to Redis for correlationId: {}", correlationId))
                .doOnError(e -> log.error("Failed to send response to Redis for correlationId: {}", correlationId, e))
                .onErrorMap(e -> new RedisResponseException("Failed to serialize or send response", e))
                .then();
    }


}