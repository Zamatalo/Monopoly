package com.example.application.redis;


import com.example.application.types.GameDTO;
import com.example.application.util.exceptions.RedisResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.stream.StreamReceiver;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.example.application.config.GameConfig.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService_Mono {
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final StreamReceiver<String, MapRecord<String, String, String>> streamReceiver;
    private final ReactiveRedisTemplate<String, Object> redisTemplate_forObjects;
    private final ObjectMapper objectMapper;

    public Mono<RecordId> publishToStream(String stream, Map<String, String> payload) {
        return redisTemplate.opsForStream()
                .add(stream, payload)
                .doOnSuccess(_ -> log.debug("Response sent for correlationId: {}", payload))
                .doOnError(e -> log.error("Failed to send response for correlationId: {}", payload, e))
                .onErrorMap(e -> new RedisResponseException("Failed to send response", e));
    }

    public Mono<Void> ensureConsumerGroupExists() {
        return redisTemplate.opsForStream()
                .createGroup(RESPONSE_STREAM, GATEWAY_GROUP)
                .onErrorResume(_ -> Mono.empty())
                .then(redisTemplate.opsForStream()
                        .createGroup(REQUEST_STREAM, BACKEND_GROUP)
                        .onErrorResume(_ -> Mono.empty())
                )
                .onErrorResume(_ -> Mono.empty())
                .then();


    }

    public Mono<Void> acknowledgeMessage(String stream, String group, RecordId recordId) {
        return redisTemplate.opsForStream()
                .acknowledge(stream, group, recordId).then();
    }

    public Flux<MapRecord<String, String, String>> listenToStream(String stream, String group, String consumerName) {
        return streamReceiver.receive(
                Consumer.from(group, consumerName),
                StreamOffset.create(stream, ReadOffset.lastConsumed())
        );
    }

    public Mono<Void> publishGameUpd(GameDTO gameDTO) {
        try {
            String json = objectMapper.writeValueAsString(gameDTO);
            return redisTemplate.convertAndSend(GAME_UPDATE_CHANNEL, json)
                    .then();
        } catch (Exception e) {
            log.error("Error when publishing Game Update: {}", e.getMessage());
            return Mono.error(e);
        }
    }


    public <T> Flux<T> listenToChannel_Object(String channel, Class<T> clazz) {
        return redisTemplate.listenTo(new ChannelTopic(channel))
                .handle((message, sink) -> {
                    String json = message.getMessage();
                    try {
                        sink.next(objectMapper.readValue(json, clazz));
                    } catch (Exception e) {
                        log.error("Error when listening to Game Update: {}", e.getMessage());
                        sink.error(e);
                    }
                });
    }


}