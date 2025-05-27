package com.example.application.handlers.lobby;

import com.example.application.entity.Player;
import com.example.application.services.GameService;
import com.example.application.services.PlayerService;
import com.example.application.util.enums.PlayerColors;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Override
    public String getAction() {
        return "JOIN_TO_GAME";
    }

    @Override
    public void handle(RequestContextRedis ctx) {
        try {
            var gameId = ctx.body().get("gameId");
            var playerId = ctx.body().get("playerId");
            var playerColor = ctx.body().get("playerColor");
            var playerName = ctx.body().get("playerName");
            playerService.findById(UUID.fromString(playerId));


            Player player = new Player();
            player.setPlayerName(playerName);
            player.setPlayerId(UUID.fromString(playerId));
            player.setColor(PlayerColors.valueOf(playerColor.toString()));

            gameService.addPlayerToGame(player, UUID.fromString(gameId));

            var payload = String.format("Player %s joined the game", playerName);
            ctx.respond(payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
