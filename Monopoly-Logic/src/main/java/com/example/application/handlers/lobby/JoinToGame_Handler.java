package com.example.application.handlers.lobby;

import com.example.application.entity.Player;
import com.example.application.services.GameService;
import com.example.application.services.PlayerService;
import com.example.application.services.RedisService;
import com.example.application.types.GameState;
import com.example.application.util.enums.PlayerColors;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class JoinToGame_Handler implements GameActionHandler {
    private final GameService gameService;
    private final PlayerService playerService;
    private final RedisService redisService;

    @Override
    public String getAction() {
        return "JOIN_TO_GAME";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            var gameId = ctx.body().get("gameId");
            var playerId = ctx.body().get("playerId");
            var playerName = ctx.body().get("playerName");
            var playerColor = ctx.body().get("playerColor");

            var existing = playerService.findById(UUID.fromString(playerId));
            if (existing.isPresent()) {
                ctx.respond("Player already exists");
                return;
            }

            var player = new Player();
            player.setPlayerId(UUID.fromString(playerId));
            player.setPlayerName(playerName);
            player.setColor(PlayerColors.valueOf(playerColor));

            var updatedGame = gameService.addPlayerToGame(player, UUID.fromString(gameId));
            redisService.publishGameUpd(gameService.findById(UUID.fromString(gameId)));
            ctx.respond(updatedGame);

        } catch (Exception e) {
            log.error("Failed to join game", e);
            ctx.respond("Internal error");
        }
    }
}
