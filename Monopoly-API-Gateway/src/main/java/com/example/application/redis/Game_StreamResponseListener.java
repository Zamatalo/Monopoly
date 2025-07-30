package com.example.application.redis;

import com.example.application.service.RedisService_Mono;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class Game_StreamResponseListener {
    private final RedisService_Mono redisService;
    private final Game_StreamRequest responseHandler;
    private Disposable subscription;

    @PostConstruct
    public void initialize() {
        redisService.ensureConsumerGroupExists()
                .doOnSuccess(v -> log.info("Consumer group ensured, starting listener"))
                .thenMany(redisService.listenToStream()
                        .flatMap(this::processResponse)
                        .doOnError(e -> log.error("Error in stream listener", e))
                )
                .subscribe();
    }



    private Mono<Void> processResponse(MapRecord<String, String, String> message) {
        return Mono.defer(() -> {
            Map<String, String> body = message.getValue();
            String correlationId = body.get("correlationId");

            if (correlationId == null) {
                log.warn("Received response without correlationId");
                return acknowledge(message);
            }
            if (body.get("payload")==null || body.get("payload").isEmpty()) {
                log.warn("Received response without payload");
                return acknowledge(message);
            }
            if (body.get("error")!=null) {
                log.warn("Received error response: {}", body.get("error"));
                return acknowledge(message);
            }
            try {
                responseHandler.complete(correlationId, body.get("payload"));
                return acknowledge(message);
            } catch (Exception e) {
                log.error("Error processing response", e);
                return acknowledge(message);
            }
        });
    }

    private Mono<Void> acknowledge(MapRecord<String, String, String> message) {
        return redisService.acknowledgeMessage(message.getId()).then();
    }

    @PreDestroy
    public void cleanup() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("Stopped listening to responses stream");
        }
    }
}