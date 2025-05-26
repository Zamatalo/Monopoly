package com.example.application.controller;

import com.example.application.redis.RedisPublisher;
import com.example.application.service.RedisRequestReplyService;
import com.example.application.types.GameActions;
import com.example.application.types.PlayerActions;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Controller
public class GatewayController {
    private final RedisPublisher publisher;
    private final RedisRequestReplyService redisRequestReplyService;

    @MutationMapping
    public void makePlayerAction(@Argument("gameId") UUID gameId,
                                 @Argument("playerId") UUID playerId,
                                 @Argument("action") PlayerActions action) {
        publisher.publish("gameId:" + gameId + ":player:" + playerId + ":action", action);
    }

    @MutationMapping
    public void makeGameAction(@Argument("gameId") UUID gameId,
                               @Argument("action") GameActions action) {
        publisher.publish("gameId:" + gameId + ":action", action);
    }

    @QueryMapping
    public CompletableFuture<List<GameActions>> getGameActions(@Argument("gameId") UUID gameId) {
        return redisRequestReplyService.sendAndReceive(
                "game.getActions", Map.of("gameId", gameId), GameActions.class);
    }

    @QueryMapping
    public CompletableFuture<List<PlayerActions>> getPlayerActions(@Argument("gameId") UUID gameId,
                                                             @Argument("playerId") UUID playerId) {
        return redisRequestReplyService.sendAndReceive(
                "game.player.getActions", Map.of("gameId", gameId, "playerId", playerId), PlayerActions.class
        );
    }

}
