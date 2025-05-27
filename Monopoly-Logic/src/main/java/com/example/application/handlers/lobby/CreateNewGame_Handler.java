package com.example.application.handlers.lobby;

import com.example.application.components.GameActionResolver;
import com.example.application.entity.Game;
import com.example.application.services.GameService;
import com.example.application.types.GameDTO;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CreateNewGame_Handler implements GameActionHandler {
    private final GameService gameService;

    @Override
    public String getAction() {
        return "CREATE_GAME";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            Game game = new Game();
            GameDTO savedGame =gameService.save(game);
            ctx.respond(savedGame);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
