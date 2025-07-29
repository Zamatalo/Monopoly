package com.example.application.redis;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.data.redis.stream.StreamReceiver;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class Game_StreamResponseListener {
    @Value("${spring.data.redis.gameRequestStream}")
    private String REQUEST_STREAM;
    @Value("${spring.data.redis.gameResponseStream}")
    private String RESPONSE_STREAM;
    @Value("${spring.data.redis.backendGroup}")
    private String BACKEND_GROUP;
    @Value("${spring.data.redis.gatewayGroup}")
    private String GATEWAY_GROUP;
    private static final String CONSUMER_NAME = "gateway";

    private final Game_StreamRequest responseHandler;
    private final StreamReceiver<String, MapRecord<String, String, String>> streamReceiver;
    private final ReactiveStreamOperations<String,String,String> operations;

    @PostConstruct
    public void init() {
        createGroupIfNotExists(REQUEST_STREAM, BACKEND_GROUP).subscribe();
        createGroupIfNotExists(RESPONSE_STREAM, GATEWAY_GROUP).subscribe();

        startReactiveStreamListener().subscribe();

    }

    private Flux<?> startReactiveStreamListener() {
        return streamReceiver.receive(Consumer.from(GATEWAY_GROUP, CONSUMER_NAME),
                        StreamOffset.create(REQUEST_STREAM, ReadOffset.lastConsumed()))
                .flatMap(this::handleMessage);
    }

    private Mono<?> createGroupIfNotExists(String stream, String group) {
        return operations
                .createGroup(stream,group)
                .doOnSuccess(v -> log.info("Created consumer group '{}'", group))
                .onErrorResume(e -> {
                    if (e.getMessage().contains("BUSYGROUP")) {
                        log.info("Consumer group '{}' already exists", group);
                        return Mono.empty();
                    }
                    return Mono.error(e);
                });

    }

    private Mono<?> handleMessage(MapRecord<String, String, String> message) {
            Map<String, String> body = message.getValue();
            String correlationId = body.get("correlationId");
            if (correlationId != null) {
                correlationId = correlationId.replace("\"", "");
            }
            String payload = body.get("payload");

            log.debug("Received message from Redis stream, correlationId: {}", correlationId);

            if (correlationId == null) {
                log.warn("Received message without correlationId: {}", payload);
                return Mono.error(new RuntimeException("CorrelationId is null"));
            }

            responseHandler.complete(correlationId, payload);
        return operations
                .acknowledge(RESPONSE_STREAM, GATEWAY_GROUP, message.getId());
    }
}