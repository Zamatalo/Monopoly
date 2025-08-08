package com.example.application.services.reactive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for interacting with the Monopoly-Dice microservice over Redis.
 * <p>
 * This service sends a dice roll request to a Redis pub/sub channel and listens
 * for the asynchronous response with the result of the dice roll.
 * <p>
 * It uses {@link ReactiveRedisTemplate} to send messages and {@link ReactiveRedisMessageListenerContainer}
 * to receive responses. Responses are expected to be in JSON format and include the game ID and dice value.
 *
 * <p><strong>Redis Channels Used:</strong>
 * <ul>
 *     <li><code>game:{gameId}:dice-roll-action</code> – to trigger a dice roll.</li>
 *     <li><code>game.diceResult</code> – to receive dice roll results.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiceService_Mono {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final Map<String, Mono<Integer>> activeRolls = new ConcurrentHashMap<>();
    private final ReactiveRedisMessageListenerContainer reactiveRedisMessageListenerContainer;
    private final ObjectMapper objectMapper;


    public Mono<Integer> rollDice(String gameId) {
        String diceResultChannel = "game.diceResult";
        String diceRollChannel = "game:" + gameId + ":dice-roll-action";

        if (activeRolls.containsKey(gameId)) {
            return activeRolls.get(gameId);
        }

        Mono<Integer> diceMono = reactiveRedisTemplate.convertAndSend(diceRollChannel, "")
                .thenMany(reactiveRedisMessageListenerContainer.receive(ChannelTopic.of(diceResultChannel)))
                .map(this::parseMessage)
                .filter(msg -> msg != null && gameId.equals(msg.gameId()))
                .map(DiceResult::diceValue)
                .next()
                .timeout(Duration.ofSeconds(10))
                .doOnError(e -> log.warn("Dice roll failed for game {}: {}", gameId, e.getMessage()))
                .doFinally(_ -> activeRolls.remove(gameId))
                .cache();

        activeRolls.put(gameId, diceMono);
        return diceMono;
    }

    private DiceResult parseMessage(ReactiveSubscription.Message<String, String> message) {
        try {
            String body = message.getMessage();
            JsonNode node = objectMapper.readTree(body);
            String gameId = node.get("gameId").asText();
            int diceResult = node.get("diceResult").asInt();
            return new DiceResult(gameId, diceResult);
        } catch (Exception e) {
            log.warn("Failed to parse dice result: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to parse dice result: " + e.getMessage());
        }
    }


    private record DiceResult(String gameId, int diceValue) {}
}