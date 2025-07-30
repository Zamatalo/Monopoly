package com.example.application.redis;

import com.example.application.services.reactive.RedisService_Mono;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameRequestStreamListener {
    private final ObjectMapper objectMapper;
    private final RedisService_Mono redisService;
    private final List<GameActionHandler> handlers;
    private Disposable subscription;

    @PostConstruct
    public void init() {
        Map<String, GameActionHandler> handlerMap = handlers.stream()
                .collect(Collectors.toMap(GameActionHandler::getAction, h -> h));

        subscription =
                redisService.ensureConsumerGroupExists()
                        .onErrorResume(e -> {
                            log.error("Error creating consumer group", e);
                            return Mono.empty();
                        })
                        .thenMany(redisService.listenToStream())
                        .flatMap(msg -> processMessage(msg, handlerMap))
                        .doOnError(e -> log.error("Error in stream listener", e))
                        .subscribe();

    }

    private Mono<Void> processMessage(MapRecord<String, String, String> msg,
                                      Map<String, GameActionHandler> handlerMap) {
       return Mono.defer(() -> {
            Map<String, String> body = msg.getValue();
            String action = body.get("action");
            String correlationId = body.get("correlationId");

            GameActionHandler handler = handlerMap.get(action);

            if (handler == null) {
                log.warn("Unknown action: {}", action);
                return sendResponse(correlationId, "Unknown action: " + action);
            }

            var ctx = new RequestContextRedis(correlationId,body,redisService,objectMapper);

            return handler.handle(ctx)
                    .onErrorResume(e -> {
                        log.error("Handler error", e);
                        return sendResponse(correlationId, e.getMessage())
                                .then(acknowledge(msg));
                    });
        });

    }

    private Mono<Void> sendResponse(String correlationId, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("correlationId", correlationId);
        response.put("error", message);
        return redisService.publishToResponseStream(response).then();
    }

    private Mono<Void> acknowledge(MapRecord<String, String, String> msg) {
        return redisService.acknowledgeMessage(msg.getId());
    }

    @PreDestroy
    public void cleanup() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("Stopped listening to responses stream");
        }
    }
}