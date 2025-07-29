//package com.example.application.redis;
//
//import com.example.application.components.GameActionResolver;
//import com.example.application.services.reactive.GameService_Mono;
//import com.example.application.services.imperative.PlayerService;
//import com.example.application.services.reactive.PlayerService_Mono;
//import com.example.application.types.GameActions;
//import com.example.application.types.GameDTO;
//import com.example.application.types.PlayerActions;
//import com.example.application.types.PlayerDTO;
//import com.example.application.utility.GameActionHandler;
//import com.example.application.utility.RequestContextRedis;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.connection.stream.*;
//import org.springframework.data.redis.core.ReactiveRedisTemplate;
//import org.springframework.data.redis.stream.StreamReceiver;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class GameRequestStreamListener {
//
//    @Value("${spring.data.redis.gameRequestStream}")
//    private String REQUEST_STREAM;
//
//    @Value("${spring.data.redis.gameResponseStream}")
//    private String RESPONSE_STREAM;
//
//    @Value("${spring.data.redis.backendGroup}")
//    private String BACKEND_GROUP;
//
//    @Value("${spring.data.redis.gatewayGroup}")
//    private String GATEWAY_GROUP;
//
//    private static final String CONSUMER_NAME = "backend-1";
//
//    private final List<GameActionHandler> handlers;
//    private final GameService_Mono gameService;
//    private final PlayerService_Mono playerService;
//    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
//    private final ObjectMapper objectMapper;
//
//    private final Map<String, GameActionHandler> handlerMap = new HashMap<>();
//
//    @PostConstruct
//    public void startListening() {
//        /// Create groups
//        createGroupIfNotExists(REQUEST_STREAM, BACKEND_GROUP).subscribe();
//        createGroupIfNotExists(RESPONSE_STREAM, GATEWAY_GROUP).subscribe();
//
//        /// Register handlers
//        for (GameActionHandler handler : handlers) {
//            handlerMap.put(handler.getAction(), handler);
//        }
//
//        startReactiveStreamListener().subscribe();
//    }
//
//
//    private Mono<?> createGroupIfNotExists(String stream, String group) {
//        return reactiveRedisTemplate.getConnectionFactory().getReactiveConnection()
//                .streamCommands()
//                .xGroupCreate(ByteBuffer.wrap(stream.getBytes(StandardCharsets.UTF_8)), group, ReadOffset.latest(), true)
//                .doOnSuccess(v -> log.info("Created consumer group '{}'", group))
//                .onErrorResume(e -> {
//                    if (e.getMessage().contains("BUSYGROUP")) {
//                        log.info("Consumer group '{}' already exists", group);
//                        return Mono.empty();
//                    }
//                    return Mono.error(e);
//                });
//    }
//
//    private Flux<?> startReactiveStreamListener() {
//        return streamReceiver
//                .receive(Consumer.from(BACKEND_GROUP, CONSUMER_NAME),
//                        StreamOffset.create(REQUEST_STREAM, ReadOffset.lastConsumed()))
//                .flatMap(this::handleMessageReactive);
//    }
//
//    private Mono<?> handleMessageReactive(MapRecord<String, String, String> message) {
//        try {
//            Map<String, String> body = message.getValue();
//            String action = body.get("action").replaceAll("^\"|\"$", "");
//            String correlationId = body.get("correlationId");
//
//            RequestContextRedis ctx = new RequestContextRedis(correlationId, body, message, reactiveRedisTemplate, objectMapper);
//            GameActionHandler handler = handlerMap.get(action);
//
//            if (handler != null && isActionValid(action, body)) {
//                return handler.handle(ctx);
//            } else {
//                ctx.respond("Invalid action");
//                log.warn("Invalid or unknown action received: {}", action);
//                return ack(message.getId());
//            }
//        } catch (Exception e) {
//            log.error("Error processing message: {}", message, e);
//            return sendError(message, e)
//                    .then(ack(message.getId()));
//        }
//    }
//
//    private Mono<Long> ack(RecordId messageId) {
//        return reactiveRedisTemplate
//                .opsForStream()
//                .acknowledge(REQUEST_STREAM, BACKEND_GROUP, messageId);
//    }
//
//    private Mono<?> sendError(MapRecord<String, String, String> message, Exception e) {
//        String correlationId = message.getValue().get("correlationId");
//        if (correlationId != null) {
//            return reactiveRedisTemplate.opsForStream().add(RESPONSE_STREAM, Map.of(
//                    "correlationId", correlationId,
//                    "payload", "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}"
//            ));
//        }
//        return Mono.empty();
//    }
//
//
//    private boolean isActionValid(String action, Map<String, String> body) {
//        try {
//            if ("getAllGames".equals(action) ||
//                    "CREATE_GAME".equals(action) ||
//                    "findGameByPlayerId".equals(action) ||
//                    "findGameById".equals(action) ||
//                    "getPlayer".equals(action)) {
//                return true;
//            }
//
//            if (body.get("gameId") != null) {
//                UUID gameId = UUID.fromString(body.get("gameId"));
//                GameDTO game = gameService.findById_Mono(gameId);
//                List<GameActions> gameActionsList = GameActionResolver.resolveGameActions(game);
//
//                if (gameActionsList.contains(GameActions.valueOf(action))) {
//                    return true;
//                }
//
//                if (body.get("playerId") != null) {
//                    PlayerDTO player = playerService.findById(UUID.fromString(body.get("playerId")));
//                    var playerActionsList = GameActionResolver.resolvePlayerActions(game, player);
//                    return playerActionsList.contains(PlayerActions.valueOf(action));
//                }
//            }
//        } catch (Exception e) {
//            log.warn("Invalid action context: {}", e.getMessage());
//        }
//        return false;
//    }
//}
