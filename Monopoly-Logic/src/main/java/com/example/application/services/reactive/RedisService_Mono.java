package com.example.application.services.reactive;

import com.example.application.types.GameDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RedisService_Mono {
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ReactiveStreamOperations<String, String, String> reactiveStreamOperations;

    @Value("${spring.data.redis.gameResponseStream}")
    private String responseStream;

    public Mono<Long> publishGameUpd(GameDTO gameDTO) {
        return redisTemplate.convertAndSend("game:gameUpdate", gameDTO);
    }

    public Mono<Long> publishTurnEnd(GameDTO gameDTO) {
        return redisTemplate.convertAndSend("game:turnEnd", gameDTO);
    }

    public Mono<Long> publishToBot(GameDTO gameDTO) {
        return redisTemplate.convertAndSend("game:botDecision", gameDTO);
    }

    public Mono<Long> publishToDiceService(String gameId) {
        return redisTemplate.convertAndSend("game:" + gameId + ":dice-roll-action", gameId);
    }

    public Mono<RecordId> answerToGateway(MapRecord<String, String, String> mapRecord) {
        return reactiveStreamOperations.add(mapRecord.withStreamKey(responseStream))
                .onErrorResume(e -> {
                    return Mono.error(e);
                });
    }

    public Mono<RecordId> requestToBackend(MapRecord<String, String, String> mapRecord) {
        return reactiveStreamOperations.add(mapRecord.withStreamKey(responseStream))
                .onErrorResume(e -> {
                    return Mono.error(e);
                });
    }
}