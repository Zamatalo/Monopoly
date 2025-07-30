package com.example.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.stream.StreamReceiver;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import util.exceptions.RedisResponseException;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService_Mono {
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final StreamReceiver<String, MapRecord<String,String,String>> streamReceiver;

    @Value("${spring.data.redis.gameRequestStream}")
    private String REQUEST_STREAM;
    @Value("${spring.data.redis.gameResponseStream}")
    private String RESPONSE_STREAM;
    @Value("${spring.data.redis.gatewayGroup}")
    private String GATEWAY_GROUP;

    private final String CONSUMER_NAME = "gateway-0";

    public Mono<RecordId> publishToRequestStream(Map<String, String> payload) {
        return redisTemplate.opsForStream()
                .add(REQUEST_STREAM, payload)
                .doOnSuccess(r -> log.debug("Response sent for correlationId: {}", payload))
                .doOnError(e -> log.error("Failed to send response for correlationId: {}", payload, e))
                .onErrorMap(e -> new RedisResponseException("Failed to send response", e));
    }

    public Mono<RecordId> publishToResponseStream(Map<String, String> payload) {
        return redisTemplate.opsForStream()
                .add(RESPONSE_STREAM, payload)
                .doOnSuccess(r -> log.debug("Response sent for correlationId: {}", payload))
                .doOnError(e -> log.error("Failed to send response for correlationId: {}", payload, e))
                .onErrorMap(e -> new RedisResponseException("Failed to send response", e));
    }

    public Mono<Void> ensureConsumerGroupExists() {
        return redisTemplate.opsForStream()
                .createGroup(RESPONSE_STREAM, GATEWAY_GROUP)
                .onErrorResume(_ -> Mono.empty())
                .then();
    }

    public Mono<Void> acknowledgeMessage(RecordId recordId) {
        return redisTemplate.opsForStream()
                .acknowledge(RESPONSE_STREAM, GATEWAY_GROUP, recordId).then();
    }

    public Flux<MapRecord<String, String, String>> listenToStream() {
        return streamReceiver.receive(
                Consumer.from(GATEWAY_GROUP, CONSUMER_NAME),
                StreamOffset.create(RESPONSE_STREAM, ReadOffset.lastConsumed())
        );
    }

}