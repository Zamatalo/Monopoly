package com.example.application.handlers.lobby;

import com.example.application.components.GameActionResolver;
import com.example.application.services.GameService;
import com.example.application.services.PlayerService;
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
public class GetPlayer_Handler implements GameActionHandler {
    private final PlayerService playerService;
    private final GameService gameService;

    @Override
    public String getAction() {
        return "getPlayer";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            var playerId = ctx.body().get("playerId");
            var player = playerService.findById(UUID.fromString(playerId));
            var game = gameService.findGameByPlayerId(UUID.fromString(playerId));
            player.setPlayerActions(GameActionResolver.resolvePlayerActions(game.get(),player));

            ctx.respond(player);
        } catch (Exception e) {
            ctx.respond("Player not found");
            log.error(e.getMessage());
        }
    }
}
