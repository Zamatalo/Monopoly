package com.example.application.services;

import com.example.application.controller.GameController;
import com.example.application.types.PlayerColors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {
    private final GameController gameController;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String payload = new String(message.getBody());
            log.debug("Received message on channel {}: {}", channel, payload);

            String eventType = channel.substring(channel.lastIndexOf(':') + 1);

            JsonNode json = objectMapper.readTree(payload);

            switch (eventType) {
                case "CREATE_GAME":
                    gameController.createNewGame();
                    break;
                case "JOIN_GAME":
                    handleJoinGame(json);
                    break;
                case "GET_GAMES":
                    gameController.getActiveGames();
                default:
                    log.warn("Unknown message type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }

    private void handleJoinGame(JsonNode json) {
        try {
            UUID gameId = UUID.fromString(json.get("gameId").asText());
            String playerName = json.get("playerName").asText();
            PlayerColors color = PlayerColors.valueOf(json.get("playerColor").asText());

            gameController.joinToGame(gameId, playerName, color);
        } catch (Exception e) {
            log.error("Failed to process JOIN_GAME message", e);
        }
    }
}