package com.example.application.redis;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.example.application.config.GameConfig.GATEWAY_GROUP;
import static com.example.application.config.GameConfig.RESPONSE_STREAM;

@Slf4j
@Component
@RequiredArgsConstructor
public class Game_StreamResponseListener {
    private final RedisService_Mono redisService;
    private final Game_StreamRequest responseHandler;
    private Disposable subscription;

    @PostConstruct
    public void initialize() {
        subscription = redisService.ensureConsumerGroupExists()
                .doOnSuccess(_ -> log.info("Consumer group ensured, starting listener"))
                .thenMany(redisService.listenToStream(
                        RESPONSE_STREAM, GATEWAY_GROUP, "gateway-0")
                                .flatMap(this::processResponse)
                        .doOnError(e -> log.error("Error in stream listener", e))).subscribe();
    }


    private Mono<Void> processResponse(MapRecord<String, String, String> message) {
        return Mono.defer(() -> {
            Map<String, String> body = message.getValue();
            String correlationId = body.get("correlationId");

            if (correlationId == null) {
                log.warn("Received response without correlationId");
                return acknowledgeMessage(message.getStream(), message.getId());
            }
            if (body.get("payload") == null || body.get("payload").isEmpty()) {
                log.warn("Received response without payload");
                return acknowledgeMessage(message.getStream(), message.getId());

            }
            if (body.get("error") != null) {
                log.warn("Received error response: {}", body.get("error"));
                return acknowledgeMessage(message.getStream(), message.getId());
            }

            try {
                responseHandler.complete(correlationId, body.get("payload"));
                return acknowledgeMessage(message.getStream(), message.getId());

            } catch (Exception e) {
                log.error("Error processing response", e);
                return acknowledgeMessage(message.getStream(), message.getId());
            }
        });
    }

    private Mono<Void> acknowledgeMessage(String stream, RecordId id) {
        return redisService.acknowledgeMessage(stream, GATEWAY_GROUP, id);
    }

    @PreDestroy
    public void cleanup() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("Stopped listening to responses stream");
        }
    }
}