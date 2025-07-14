package com.example.application.handlers.lobby;

import com.example.application.services.reactive.GameService_Mono;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GetAllGames_Handler implements GameActionHandler {
    private final GameService_Mono gameService;

    @Override
    public String getAction() {
        return "getAllGames";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            var games = gameService.findAll_Mono();
            ctx.respond(games);
        } catch (Exception e) {
            ctx.respond("Internal Server Error");
            log.error(e.getMessage());
        }
    }
}
