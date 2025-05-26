package com.example.application.handlers;

import com.example.application.entity.Game;
import com.example.application.services.GameService;
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
    private final ObjectMapper objectMapper;

    @Override
    public String getAction() {
        return "createNewGame";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            Game game = new Game();
            gameService.save(game);
            ctx.respond(objectMapper.writeValueAsString(game));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
