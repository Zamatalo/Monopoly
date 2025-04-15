package com.example.application.redis;

import com.example.application.types.GameDTO;
import com.example.application.types.PlayerColors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessagePublisher {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private List<GameDTO> games = new ArrayList<>();

    public void publishCreateGame() {
        try {
            Map<String, String> payload = Map.of("event", "CREATE_GAME");
            String message = objectMapper.writeValueAsString(payload);
            log.info("Publishing CREATE_GAME event");
            redisTemplate.convertAndSend("games:events:CREATE_GAME", message);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize create game payload", e);
            throw new RuntimeException("Failed to publish create game event", e);
        }
    }

    public void publishJoinGame(UUID gameId, String playerName, PlayerColors color) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("gameId", gameId.toString());
            payload.put("playerName", playerName);
            payload.put("playerColor", color.toString());

            String message = objectMapper.writeValueAsString(payload);
            log.info("Publishing JOIN_GAME event: {}", message);
            redisTemplate.convertAndSend("games:events:JOIN_GAME", message);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize join game payload", e);
            throw new RuntimeException("Failed to publish join game event", e);
        }
    }
}