package com.example.application.handlers.lobby;

import com.example.application.services.GameService;
import com.example.application.services.RedisService;
import com.example.application.services.TurnService;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EndTurn_Handler implements GameActionHandler {
    private final TurnService turnService;
    private final GameService gameService;
    private final RedisService redisService;

    @Override
    public String getAction() {
        return "END_TURN";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            UUID gameId = UUID.fromString(ctx.body().get("gameId"));


            var updatedGame = gameService.findById(gameId);
            redisService.publishGameUpd(updatedGame);
            turnService.endTurn(gameId);
            ctx.respond(updatedGame);
        } catch (Exception e) {
            log.error("Failed to end turn", e);
            ctx.respond("Internal error");
        }
    }
}