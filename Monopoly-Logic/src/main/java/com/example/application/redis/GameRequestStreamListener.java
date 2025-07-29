package com.example.application.redis;

import com.example.application.components.GameActionResolver;
import com.example.application.services.reactive.GameService_Mono;
import com.example.application.services.reactive.PlayerService_Mono;
import com.example.application.types.GameActions;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerActions;
import com.example.application.types.PlayerDTO;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.ReactiveStreamCommands;
import org.springframework.data.redis.connection.Subscription;
import org.springframework.data.redis.connection.SubscriptionListener;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.stream.StreamReceiver;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameRequestStreamListener {

    @Value("${spring.data.redis.gameRequestStream}")
    private String REQUEST_STREAM;

    @Value("${spring.data.redis.gameResponseStream}")
    private String RESPONSE_STREAM;

    @Value("${spring.data.redis.backendGroup}")
    private String BACKEND_GROUP;

    @Value("${spring.data.redis.gatewayGroup}")
    private String GATEWAY_GROUP;

    private static final String CONSUMER_NAME = "backend";

    private final List<GameActionHandler> handlers;
    private final GameService_Mono gameService;
    private final PlayerService_Mono playerService;
    private final ObjectMapper objectMapper;
    private final StreamReceiver <String, MapRecord<String, String,String>> streamReceiver;
    private final ReactiveStreamOperations<String,String,String> streamOperations;
    private Disposable streamSubscription;

    private final Map<String, GameActionHandler> handlerMap = new HashMap<>();

    @PostConstruct
    public void startListening() {
        /// Create groups
        createGroupIfNotExists(REQUEST_STREAM, BACKEND_GROUP).subscribe();
        createGroupIfNotExists(RESPONSE_STREAM, GATEWAY_GROUP).subscribe();

        /// Register handlers
        for (GameActionHandler handler : handlers) {
            handlerMap.put(handler.getAction(), handler);
        }
        startReactiveStreamListener().subscribe();
    }

    @PreDestroy
    public void stopListening() {
        if (streamSubscription != null && !streamSubscription.isDisposed()) {
            streamSubscription.dispose();
        }
    }


    private Mono<?> createGroupIfNotExists(String stream, String group) {
        return streamOperations
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

    private Flux<?> startReactiveStreamListener() {
        return streamReceiver
                .receive(Consumer.from(BACKEND_GROUP,CONSUMER_NAME),
                        StreamOffset.create(REQUEST_STREAM,ReadOffset.lastConsumed()))
                .flatMap(this::handleMessageReactive);
    }

    private Mono<?> handleMessageReactive(MapRecord<String, String, String> message) {
        Map<String, String> body = message.getValue();
        String action = body.get("action").replaceAll("^\"|\"$", "");
        String correlationId = body.get("correlationId");

        RequestContextRedis ctx = new RequestContextRedis(correlationId, body, message, streamOperations, objectMapper);
        GameActionHandler handler = handlerMap.get(action);

        if (handler == null) {
            log.warn("Unknown action: {}", action);
            return ctx.respond("Invalid action").then(ack(message.getId()));
        }

        return isActionValid(action, body)
                .flatMap(valid -> {
                    if (valid) {
                        return handler.handle(ctx)
                                .then(ack(message.getId()));
                    } else {
                        log.warn("Invalid action context for action: {}", action);
                        return ctx.respond("Invalid action").then(ack(message.getId()));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error processing message: {}", message, e);
                    return sendError(message, new IllegalArgumentException("Error processing message: " + message))
                            .then(ack(message.getId()));
                });
    }


    private Mono<Long> ack(RecordId messageId) {
        return streamOperations
                .acknowledge(REQUEST_STREAM, BACKEND_GROUP, messageId);
    }

    private Mono<?> sendError(MapRecord<String, String, String> message, Exception e) {
        String correlationId = message.getValue().get("correlationId");
        if (correlationId != null) {
            return streamOperations.add(RESPONSE_STREAM, Map.of(
                    "correlationId", correlationId,
                    "payload", "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}"
            ));
        }
        return Mono.empty();
    }


    private Mono<Boolean> isActionValid(String action, Map<String, String> body) {
        try {
            if ("getAllGames".equals(action) ||
                    "CREATE_GAME".equals(action) ||
                    "findGameByPlayerId".equals(action) ||
                    "findGameById".equals(action) ||
                    "getPlayer".equals(action)) {
                return Mono.just(true);
            }

            if (body.get("gameId") != null) {
                UUID gameId = UUID.fromString(body.get("gameId"));
                UUID playerId = body.get("playerId") != null ? UUID.fromString(body.get("playerId")) : null;

                return gameService.findById_Mono(gameId)
                        .flatMap(game -> {
                            List<GameActions> gameActionsList = GameActionResolver.resolveGameActions(game);

                            if (gameActionsList.contains(GameActions.valueOf(action))) {
                                return Mono.just(true);
                            }

                            if (playerId != null) {
                                return playerService.findById(playerId)
                                        .map(player -> {
                                            List<PlayerActions> playerActionsList = GameActionResolver.resolvePlayerActions(game, player);
                                            return playerActionsList.contains(PlayerActions.valueOf(action));
                                        });
                            }

                            return Mono.just(false);
                        })
                        .onErrorResume(e -> {
                            log.warn("Error validating action: {}", e.getMessage());
                            return Mono.just(false);
                        });
            }

        } catch (Exception e) {
            log.warn("Invalid action context: {}", e.getMessage());
        }

        return Mono.just(false);
    }

}
