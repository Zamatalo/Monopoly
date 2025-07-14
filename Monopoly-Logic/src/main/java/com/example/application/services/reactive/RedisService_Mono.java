package com.example.application.services.reactive;

import com.example.application.types.GameDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisService_Mono {
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ReactiveStreamOperations<String, String, Object> reactiveStreamOperations;

    @Value("${spring.data.redis.gameResponseStream}")
    private String RESPONSE_STREAM;

    public Mono<Long> publishGameUpd(GameDTO gameDTO) {
        String channel = "game:gameUpdate";
        return redisTemplate.convertAndSend(channel, gameDTO);
    }

    public Mono<Long> publishTurnEnd(GameDTO gameDTO) {
        String channel = "game:turnEnd";
        return redisTemplate.convertAndSend(channel, gameDTO);
    }

    public Mono<Long> publishToBot(GameDTO gameDTO) {
        String channel = "game:botDecision";
        return redisTemplate.convertAndSend(channel, gameDTO);
    }

    public Mono<Long> publishToDiceService(String gameId) {
        String channel = "game:" + gameId + ":dice-roll-action";
        return redisTemplate.convertAndSend(channel, gameId);
    }

    public Mono<RecordId> answerToGateway(MapRecord<String,String,String> mapRecord) {
        mapRecord.withStreamKey(RESPONSE_STREAM);
        return reactiveStreamOperations.add(mapRecord);
    }

    public Mono<RecordId> requestToBackend(MapRecord<String,String,String> mapRecord) {
        mapRecord.withStreamKey(RESPONSE_STREAM);
        return reactiveStreamOperations.add(mapRecord);
    }

}
