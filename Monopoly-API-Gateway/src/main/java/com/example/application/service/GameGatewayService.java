package com.example.application.service;

import com.example.application.redis.Game_StreamRequest;
import com.example.application.redis.RedisService_Mono;
import com.example.application.types.GameActions;
import com.example.application.types.PlayerActions;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.example.application.config.GameConfig.REQUEST_STREAM;
/**
 * Service for sending game actions via Redis streams and receiving typed responses.
 * <p>
 * Publishes action requests to a Redis stream.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class GameGatewayService {
    private final RedisService_Mono redisService;
    private final Game_StreamRequest responseHandler;
    private final ObjectMapper objectMapper;


    public <T> Mono<T> sendAction(String action, Class<T> clazz) {
        return sendAction(action, new HashMap<>(), clazz);
    }

    /**
     * Sends an action with optional additional arguments to the game request stream,
     * assigning a unique correlation ID for response matching.
     *
     * @param action the action to send with type of {@link GameActions} or {@link PlayerActions}
     * @param args   additional parameters to include in the action request (e.g., playerId, gameId)
     * @param clazz  the class to map the asynchronous response to
     * @param <T>    the response type
     */
    public <T> Mono<T> sendAction(String action, Map<String, String> args, Class<T> clazz) {
        String correlationId = UUID.randomUUID().toString();

        Map<String, String> body = new HashMap<>(args);
        body.put("correlationId", correlationId);
        body.put("action", action);

        var javaType = objectMapper.getTypeFactory().constructType(clazz);

        return redisService.publishToStream(REQUEST_STREAM, body)
                .then(Mono.fromFuture(responseHandler.register(correlationId, javaType)));
    }

}
