package com.example.application.handlers.lobby;

import com.example.application.services.reactive.GameService_Mono;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FindGame_ByPlayerId_Handler implements GameActionHandler {
    private final GameService_Mono gameService;

    @Override
    public String getAction() {
        return "findGameByPlayerId";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            var playerId = ctx.body().get("playerId");
            var game = gameService.findGameByPlayerId_Mono(UUID.fromString(playerId));

            ctx.respond(game);
        } catch (Exception e) {
            ctx.respond("Internal Server Error");
            log.error(e.getMessage());
        }
    }
}
