package com.example.application.redis;

import com.example.application.components.DicePublisher;
import com.example.application.controller.GameController;
import com.example.application.controller.GameSubscription;
import com.example.application.types.DicePosition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.pubsub.RedisPubSubListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements RedisPubSubListener<String, String> {
    private final DicePublisher dicePublisher;
    private final ObjectMapper objectMapper;

    @Override
    public void message(String channel, String message) {
        try {
            String[] parts = channel.split(":");
            if (parts.length < 3) {
                log.error("Invalid channel format: {}", channel);
                return;
            }
            String gameId = parts[1];

            /**
             * listening for updates from dice-server, and publishing it to subscribed players
             *
             */
            if (channel.endsWith(":dice-update")) {
                JsonNode jsonNode = objectMapper.readTree(message);
                DicePosition position = DicePosition.newBuilder()
                        .pos(jsonNode.get("pos").toString())
                        .rot(jsonNode.get("rot").toString())
                        .build();
                dicePublisher.publish(gameId, position);
            }

            /**
             * listening for topFace and completing the future in
             * @see GameController.rollDice
             */
            if (channel.endsWith(":dice-topFace")) {
                GameController.completeDiceFuture(UUID.fromString(gameId), objectMapper.readTree(message).get("value").asInt());
            }
        } catch (Exception e) {
            log.error("Error processing Redis message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void message(String pattern, String channel, String message) {
        message(channel, message);
    }

    @Override
    public void subscribed(String channel, long count) {
        log.info("Subscribed to channel: {}", channel);
    }

    @Override
    public void psubscribed(String pattern, long count) {
        log.info("Subscribed to pattern: {}", pattern);
    }

    @Override
    public void unsubscribed(String channel, long count) {
        log.info("Unsubscribed from channel: {}", channel);
    }

    @Override
    public void punsubscribed(String pattern, long count) {
        log.info("Unsubscribed from pattern: {}", pattern);
    }
}