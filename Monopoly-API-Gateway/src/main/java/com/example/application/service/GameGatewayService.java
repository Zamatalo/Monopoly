package com.example.application.service;

import com.example.application.redis.Game_StreamRequest;
import com.example.application.redis.RedisService_Mono;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.example.application.config.GameConfig.REQUEST_STREAM;

@Service
@RequiredArgsConstructor
public class GameGatewayService {
    private final RedisService_Mono redisService;
    private final Game_StreamRequest responseHandler;
    private final ObjectMapper objectMapper;


    public <T> Mono<T> sendAction(String action, Class<T> clazz) {
        return sendAction(action, new HashMap<>(), clazz);
    }
    /// @param action will be sent via redis streams to game.request channel
    /// @param args additional info like playerId or gameId
    /// @param clazz specifies response class. (it will be later mapped in RedisStreamResponseHandler.complete())
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
