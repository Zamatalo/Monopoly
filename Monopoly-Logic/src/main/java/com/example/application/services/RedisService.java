package com.example.application.services;

import com.example.application.types.GameDTO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public void publishGameUpd(GameDTO gameDTO) {
        var channel = "game:gameUpdate";
        redisTemplate.convertAndSend(channel, gameDTO).subscribe();
    }
    public void publishTurnEnd(GameDTO gameDTO) {
        var channel = "game:turnEnd";
        redisTemplate.convertAndSend(channel, gameDTO).subscribe();
    }
    public void publishToBot(GameDTO gameDTO) {
        var channel = "game:botDecision";
        redisTemplate.convertAndSend(channel, gameDTO).subscribe();
    }
}
