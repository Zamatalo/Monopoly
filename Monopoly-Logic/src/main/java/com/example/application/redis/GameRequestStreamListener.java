package com.example.application.redis;

import com.example.application.components.GameActionResolver;
import com.example.application.services.reactive.GameService_Mono;
import com.example.application.services.reactive.PlayerService_Mono;
import com.example.application.types.GameActions;
import com.example.application.types.PlayerActions;
import com.example.application.utility.GameActionHandler;
import com.example.application.components.RequestContextRedis;
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
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.application.config.GameConfig.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameRequestStreamListener {
    private final ObjectMapper objectMapper;
    private final GameService_Mono gameService;
    private final PlayerService_Mono playerService;
    private final RedisService_Mono redisService;
    private final List<GameActionHandler> handlers;
    private Disposable subscription;

    @PostConstruct
    public void init() {
        //populating list with all available handlers
        Map<String, GameActionHandler> handlerMap = handlers.stream()
                .collect(Collectors.toMap(GameActionHandler::getAction, h -> h));

        subscription =
                redisService.ensureConsumerGroupExists()
                        .onErrorResume(e -> {
                            log.error("Error creating consumer group", e);
                            return Mono.empty();
                        })
                        .thenMany(redisService.listenToStream(REQUEST_STREAM,BACKEND_GROUP,"backend-0"))
                        .flatMap(msg -> processMessage(msg, handlerMap))
                        .doOnError(e -> log.error("Error in stream listener", e))
                        .subscribe();

    }

    private Mono<Void> processMessage(MapRecord<String, String, String> msg,
                                      Map<String, GameActionHandler> handlerMap) {
        return Mono.defer(() -> {
            var body = msg.getValue();
            var actionStr = body.get("action");
            var correlationId = body.get("correlationId");

            var handler = handlerMap.get(actionStr);
            if (handler == null) {
                log.warn("Unknown or unhandled action: {}", actionStr);
                return sendResponse(correlationId, "Unknown or unhandled action: " + actionStr)
                        .then(acknowledge(msg));
            }

            return isActionValid(actionStr, body)
                    .flatMap(isValid -> {
                        if (!isValid) {
                            log.warn("Invalid action '{}' for given context", actionStr);
                            return sendResponse(correlationId, "Invalid action: " + actionStr)
                                    .then(acknowledge(msg));
                        }

                        var ctx = new RequestContextRedis(correlationId, body, redisService, objectMapper);
                        return handler.handle(ctx)
                                .onErrorResume(e -> sendResponse(correlationId, e.getMessage()))
                                .then(acknowledge(msg));
                    });
        });
    }


    private Mono<Void> sendResponse(String correlationId, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("correlationId", correlationId);
        response.put("error", message);
        return redisService.publishToStream(RESPONSE_STREAM,response)
                .then();
    }

    private Mono<Void> acknowledge(MapRecord<String, String, String> msg) {
        return redisService.acknowledgeMessage(msg.getStream(),BACKEND_GROUP,msg.getId());
    }

    private Mono<Boolean> isActionValid(String action, Map<String, String> body) {
        if (GameActions.GET_ALL_GAMES.toString().equals(action) ||
                GameActions.CREATE_GAME.toString().equals(action) ||
                GameActions.FIND_GAME_PLAYER_ID.toString().equals(action) ||
                GameActions.FIND_GAME_BY_ID.toString().equals(action) ||
                GameActions.GET_PLAYER.toString().equals(action)) {
            return Mono.just(true);
        }

        String gameIdStr = body.get("gameId");
        if (gameIdStr == null) return Mono.just(false);

        UUID gameId;
        try {
            gameId = UUID.fromString(gameIdStr);
        } catch (IllegalArgumentException e) {
            return Mono.just(false);
        }

        return gameService.findById_Mono(gameId)
                .flatMap(game -> {
                    List<GameActions> gameActionsList = GameActionResolver.resolveGameActions(game);
                    try {
                        GameActions gameAction = GameActions.valueOf(action);
                        if (gameActionsList.contains(gameAction)) {
                            return Mono.just(true);
                        }
                    } catch (IllegalArgumentException ignored) {}

                    String playerIdStr = body.get("playerId");
                    if (playerIdStr == null) {
                        return Mono.just(false);
                    }

                    try {
                        UUID playerId = UUID.fromString(playerIdStr);
                        return playerService.findById(playerId)
                                .map(player -> {
                                    var playerActionsList = GameActionResolver.resolvePlayerActions(game, player);
                                    try {
                                        var playerAction = PlayerActions.valueOf(action);
                                        return playerActionsList.contains(playerAction);
                                    } catch (IllegalArgumentException e) {
                                        return false;
                                    }
                                });
                    } catch (IllegalArgumentException e) {
                        return Mono.just(false);
                    }
                })
                .onErrorResume(_ -> Mono.just(false));
    }

    @PreDestroy
    public void cleanup() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("Stopped listening to responses stream");
        }
    }
}