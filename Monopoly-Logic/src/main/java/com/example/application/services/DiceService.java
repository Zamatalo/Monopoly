package com.example.application.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class DiceService implements MessageListener {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisMessageListenerContainer redisListenerContainer;
    private final Map<String, CompletableFuture<Integer>> pendingRolls = new ConcurrentHashMap<>();
    private final ScheduledExecutorService timeoutExecutor = Executors.newScheduledThreadPool(4);
    private final ObjectMapper objectMapper;

    public CompletableFuture<Integer> rollDice(String gameId) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        pendingRolls.put(gameId, future);

        redisListenerContainer.addMessageListener(this,
                new ChannelTopic("game:diceResult"));

        redisTemplate.convertAndSend("game:" + gameId + ":dice-roll-action", "roll");

        timeoutExecutor.schedule(() -> {
            if (!future.isDone()) {
                future.completeExceptionally(new TimeoutException("Dice roll timeout"));
                pendingRolls.remove(gameId);
                redisListenerContainer.removeMessageListener(this);
            }
        }, 10, TimeUnit.SECONDS);

        return future;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(json);

            String gameId = node.get("gameId").asText();
            int diceResult = node.get("diceResult").asInt();
            handleDiceResult(gameId, diceResult);
            redisListenerContainer.removeMessageListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDiceResult(String gameId, int result) {
        CompletableFuture<Integer> future = pendingRolls.remove(gameId);
        if (future != null) {
            future.complete(result);
        }
    }
}