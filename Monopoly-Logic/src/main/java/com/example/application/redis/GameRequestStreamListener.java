package com.example.application.redis;

import com.example.application.components.GameActionResolver;
import com.example.application.services.GameService;
import com.example.application.services.PlayerService;
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
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

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
    private static final String CONSUMER_NAME = "backend-1";

    private final List<GameActionHandler> handlers;
    private final GameService gameService;
    private final PlayerService playerService;
    private final RedisTemplate<String, Object> redisOp;
    private final Map<String, GameActionHandler> handlerMap = new HashMap<>();
    private final ObjectMapper objectMapper;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @PostConstruct
    public void startListening() {
        /// creating groups
        createConsumerGroup(REQUEST_STREAM, BACKEND_GROUP);
        createConsumerGroup(RESPONSE_STREAM, GATEWAY_GROUP);

        /// adding handlers
        for (GameActionHandler handler : handlers) {
            handlerMap.put(handler.getAction(), handler);
        }

        /// creating listening container
        assert redisOp.getConnectionFactory() != null;
        container =
                StreamMessageListenerContainer.create(
                        redisOp.getConnectionFactory(),
                        StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                                .builder()
                                .pollTimeout(Duration.ofSeconds(1))
                                .build()
                );

        container.receive(
                Consumer.from(BACKEND_GROUP, CONSUMER_NAME),
                StreamOffset.create(REQUEST_STREAM, ReadOffset.lastConsumed()),
                this::handleMessage
        );

        container.start();
    }

    private void handleMessage(MapRecord<String, String, String> message) {
        try {
            Map<String, String> body = message.getValue();
            String action = body.get("action");
            String correlationId = body.get("correlationId");

            RequestContextRedis ctx = new RequestContextRedis(correlationId, body, message, redisOp, objectMapper);
            GameActionHandler handler = handlerMap.get(action);

            if (handler != null && isActionValid(action, body)) {
                handler.handle(ctx);
            } else {
                ctx.respond("Invalid action");
                log.warn("Invalid or unknown action received: {}", action);
            }
        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);

            String correlationId = message.getValue().get("correlationId");
            if (correlationId != null) {
                redisOp.opsForStream().add(RESPONSE_STREAM,
                        Map.of(
                                "correlationId", correlationId,
                                "payload", "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}"
                        ));
            }
        } finally {
            redisOp.opsForStream().acknowledge(REQUEST_STREAM, BACKEND_GROUP, message.getId());
        }
    }

    private void createConsumerGroup(String stream, String group) {
        try {
            redisOp.opsForStream()
                    .createGroup(stream, ReadOffset.latest(), group);
        } catch (Exception e) {
            if (!e.getMessage().contains("BUSYGROUP")) {
                System.out.println("Group exist already. Skipping");
            } else {
                System.out.println("Creating Redis group");
            }
        }
    }

    private boolean isActionValid(String action, Map<String, String> body) {
        if ("getAllGames".equals(action) ||
                "CREATE_GAME".equals(action) ||
                "findGameByPlayerId".equals(action) ||
                "findGameById".equals(action) ||
                "getPlayer".equals(action)
        ){
            return true;
        }
        /// first checking gameActions then playerActions
        if (body.get("gameId") != null) {
            UUID gameId = UUID.fromString(body.get("gameId"));
            Optional<GameDTO> game = gameService.findById(gameId);
            if (game.isEmpty()) return false;

            List<GameActions> gameActionsList = GameActionResolver.resolveGameActions(game.get());
            try {
                GameActions gameAction = GameActions.valueOf(action);
                if (gameActionsList.contains(gameAction)) {
                    return true;
                }
            } catch (IllegalArgumentException _) {
            }

            if (body.get("playerId") != null) {
                Optional<PlayerDTO> player = playerService.findById(UUID.fromString(body.get("playerId")));
                if (player.isEmpty()) return false;

                List<PlayerActions> playerActionsList = GameActionResolver.resolvePlayerActions(game.get(), player.get());
                try {
                    PlayerActions playerAction = PlayerActions.valueOf(action);
                    return playerActionsList.contains(playerAction);
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        }
        return false;
    }


    @PreDestroy
    public void stop() {
        if (container != null) {
            container.stop();
        }
    }
}
