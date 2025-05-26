package com.example.application.redis;

import com.example.application.utility.RequestContextRedis;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;
import com.example.application.utility.GameActionHandler;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameRequestStreamListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final List<GameActionHandler> handlers;

    private final Map<String, GameActionHandler> handlerMap = new HashMap<>();

    @PostConstruct
    public void startListening() {
        for (var handler : handlers) {
            handlerMap.put(handler.getAction(), handler);
        }

        assert redisTemplate.getConnectionFactory() != null;
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(
                        redisTemplate.getConnectionFactory(),
                        StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                                .pollTimeout(Duration.ofSeconds(1))
                                .build()
                );

        container.receive(
                Consumer.from("backend", "backend-1"),
                StreamOffset.create("game.request", ReadOffset.lastConsumed()),
                this::handleMessage
        );

        container.start();
    }

    private void handleMessage(MapRecord<String, String, String> message) {
        var body = message.getValue();
        var action = body.get("action");
        var correlationId = body.get("correlationId");

        var ctx = new RequestContextRedis(correlationId, body, message, redisTemplate, objectMapper);

        var handler = handlerMap.get(action);
        if (handler != null) {
            handler.handle(ctx);
        } else {
            log.error("Unknown action {}", action);
        }

        redisTemplate.opsForStream().acknowledge("game.request", "backend", message.getId());
    }
}
