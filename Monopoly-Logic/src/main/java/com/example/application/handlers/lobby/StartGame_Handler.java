package com.example.application.handlers.lobby;

import com.example.application.services.GameService;
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

    @Override
    public String getAction() {
        return "startGame";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            UUID gameId = UUID.fromString(ctx.body().get("gameID"));
            GameDTO game = gameService.startGame(gameId);
            ctx.respond(game);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
