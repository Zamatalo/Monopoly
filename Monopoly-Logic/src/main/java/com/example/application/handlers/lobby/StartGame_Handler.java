package com.example.application.handlers.lobby;

import com.example.application.services.GameService;
import com.example.application.services.RedisService;
import com.example.application.types.GameDTO;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartGame_Handler implements GameActionHandler {
    private final GameService gameService;
    private final RedisService redisService;

    @Override
    public String getAction() {
        return "START_GAME";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            UUID gameId = UUID.fromString(ctx.body().get("gameId"));
            GameDTO game = gameService.startGame(gameId);
            redisService.publishGameUpd(gameService.findById(gameId).get());
            ctx.respond(game);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
