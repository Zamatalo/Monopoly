package com.example.application.redis;

import com.example.application.request.RedisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class GameResponseStreamListener implements SmartLifecycle {
    private static final String RESPONSE_STREAM = "game.response";
    private static final String CONSUMER_GROUP = "gateway-group";
    private static final String CONSUMER_NAME = "gateway-1";

    private final RedisStreamResponseHandler responseHandler;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
    private boolean running = false;

    @Override
    public void start() {
        running = true;
        initializeContainer();
    }

    private void initializeContainer() {
        try {

            StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                    StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                            .builder()
                            .pollTimeout(Duration.ofSeconds(1))
                            .build();

            assert redisTemplate.getConnectionFactory() != null;
            container = StreamMessageListenerContainer.create(
                    redisTemplate.getConnectionFactory(),
                    options
            );

            // Подписываемся на поток
            container.receive(
                    Consumer.from(CONSUMER_GROUP, CONSUMER_NAME),
                    StreamOffset.create(RESPONSE_STREAM, ReadOffset.lastConsumed()),
                    this::handleMessage
            );

            container.start();
            log.info("Started listening to Redis stream: {}", RESPONSE_STREAM);
        } catch (Exception e) {
            log.error("Failed to initialize Redis stream container", e);
            restartWithDelay();
        }
    }

    private void handleMessage(MapRecord<String, String, String> message) {
        try {
            Map<String, String> body = message.getValue();
            String correlationId = body.get("correlationId");
            if (correlationId != null) {
                correlationId = correlationId.replace("\"", "");
            }
            String payload = body.get("payload");

            log.debug("Received message from Redis stream, correlationId: {}", correlationId);

            if (correlationId == null) {
                log.warn("Received message without correlationId: {}", payload);
                return;
            }

            responseHandler.complete(correlationId, payload);
        } catch (Exception e) {
            log.error("Error processing Redis stream message", e);
        } finally {
            redisTemplate.opsForStream().acknowledge(RESPONSE_STREAM, CONSUMER_GROUP, message.getId());
        }
    }

    private void restartWithDelay() {
        stopContainer();
        scheduler.execute(() -> {
            try {
                Thread.sleep(5000);
                if (running) {
                    log.info("Attempting to restart Redis stream container...");
                    initializeContainer();
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        });
    }

    @Override
    public void stop() {
        running = false;
        stopContainer();
        scheduler.shutdownNow();
    }

    private void stopContainer() {
        if (container != null && container.isRunning()) {
            container.stop();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}