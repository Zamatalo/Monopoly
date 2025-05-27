package com.example.application.redis;

import com.example.application.redis.RedisStreamResponseHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GameResponseStreamListener {
    private final RedisStreamResponseHandler responseHandler;
    private final RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void startListening() {
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(
                        redisTemplate.getConnectionFactory(),
                        StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                                .pollTimeout(Duration.ofSeconds(1))
                                .build()
                );

        container.receive(
                Consumer.from("gateway", "gateway-1"),
                StreamOffset.create("game.response", ReadOffset.lastConsumed()),
                this::handleMessage
        );

        container.start();
    }

    private void handleMessage(MapRecord<String, String, String> message) {
        Map<String, String> body = message.getValue();
        String correlationId = body.get("correlationId");
        String response = body.get("payload");

        if (response.equals("\"Invalid action\"")) {
            throw new RuntimeException("Invalid/Forbidden action");
        }
        responseHandler.complete(correlationId, response);
        redisTemplate.opsForStream().acknowledge("game.response", "gateway", message.getId());
    }
}
