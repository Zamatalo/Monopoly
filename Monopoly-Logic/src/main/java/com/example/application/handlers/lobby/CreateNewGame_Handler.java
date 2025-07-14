package com.example.application.handlers.lobby;

import com.example.application.entity.Game;
import com.example.application.services.reactive.GameService_Mono;
import com.example.application.types.GameDTO;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CreateNewGame_Handler implements GameActionHandler {
    private final GameService_Mono gameService;

    @Override
    public String getAction() {
        return "CREATE_GAME";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            Game game = new Game();
            GameDTO savedGame = gameService.save_Mono(game);
            ctx.respond(savedGame);
        } catch (Exception e) {
            ctx.respond("Internal Server Error");
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
