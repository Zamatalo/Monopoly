package com.example.application.redis;

import com.example.application.components.GameActionResolver;
import com.example.application.services.GameService;
import com.example.application.services.PlayerService;
import com.example.application.types.GameActions;
import com.example.application.types.GameDTO;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GameRequestStreamListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final List<GameActionHandler> handlers;
    private final Map<String, GameActionHandler> handlerMap = new HashMap<>();
    private final GameService gameService;
    private final PlayerService playerService;

    @PostConstruct
    public void startListening() {
        //filling with all handlers
        for (var handler : handlers) {
            handlerMap.put(handler.getAction(), handler);
        }
        //creating groups
        try {
            redisTemplate.opsForStream()
                    .createGroup("game.request", ReadOffset.latest(), "backend");
        } catch (Exception e) {
            if (!e.getMessage().contains("BUSYGROUP")) {
                System.out.println("Group exist already. Skipping");
            }
        }

        try {
            redisTemplate.opsForStream()
                    .createGroup("game.response", ReadOffset.latest(), "gateway");
        } catch (Exception e) {
            if (!e.getMessage().contains("BUSYGROUP")) {
                System.out.println("Group exist already. Skipping");
            }
        }

        assert redisTemplate.getConnectionFactory() != null;
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(
                        redisTemplate.getConnectionFactory(),
                        StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                                .pollTimeout(Duration.ofSeconds(1))
                                .build()
                );

        container.receive(
                Consumer.from("backend", "backend-1"),
                StreamOffset.create("game.request", ReadOffset.lastConsumed()),
                this::handleMessage
        );

        container.start();
    }

    private void handleMessage(MapRecord<String, String, String> message) {
        var body = message.getValue();
        var action = body.get("action");
        var correlationId = body.get("correlationId");

        var ctx = new RequestContextRedis(correlationId, body, message, redisTemplate, objectMapper);

        var handler = handlerMap.get(action);
        if (handler != null && isActionValid(action, body)) {
            handler.handle(ctx);
        } else {
            ctx.respond("Invalid action");
            log.error("Unknown/Forbidden action {}", action);
        }

        redisTemplate.opsForStream().acknowledge("game.request", "backend", message.getId());
    }

    private boolean isActionValid(String action, Map<String, String> body) {
        if (action.equals("getAllGames") ||
                action.equals("createNewGame") ||
                action.equals("findGameByPlayerId") ||
                action.equals("findGameById") ||
                action.equals("getPlayer")) {
            return true;
        }

        if (body.get("gameId") != null && !action.isEmpty()) {
            var gameId = body.get("gameId");
            Optional<GameDTO> game = gameService.findById(UUID.fromString(gameId));
            assert game.isPresent();
            List<GameActions> actions = GameActionResolver.resolveGameActions(game.get());
            return actions.contains(GameActions.valueOf(action));
        }

//        if (body.get("gameId") != null && !action.isEmpty() && body.get("playerId") != null) {
//            var gameId = body.get("gameId");
//            var playerId = body.get("playerId");
//
//            Optional<GameDTO> game = gameService.findById(UUID.fromString(gameId));
//            assert game.isPresent();
//
//            PlayerDTO player = playerService.findById(UUID.fromString(playerId));
//
//            List<PlayerActions> actions = GameActionResolver.resolvePlayerActions(game.get(), player);
//            return actions.contains(PlayerActions.valueOf(action));
//        }

        return false;
    }
}
