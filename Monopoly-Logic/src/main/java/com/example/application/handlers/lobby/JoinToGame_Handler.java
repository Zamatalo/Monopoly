package com.example.application.handlers.lobby;

import com.example.application.components.RequestContextRedis;
import com.example.application.entity.Player;
import com.example.application.services.reactive.GameService_Mono;
import com.example.application.services.reactive.PlayerService_Mono;
import com.example.application.types.GameActions;
import com.example.application.types.PlayerColors;
import com.example.application.utility.GameActionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;
/**
 *Handler responsible for processing the {@link GameActions#JOIN_TO_GAME} game action.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JoinToGame_Handler implements GameActionHandler {
    private final GameService_Mono gameService;
    private final PlayerService_Mono playerService;

    @Override
    public String getAction() {
        return GameActions.JOIN_TO_GAME.toString();
    }

    @Override
    public Mono<Void> handle(RequestContextRedis ctx) {
        try {
            var gameIdStr = ctx.body().get("gameId");
            var playerIdStr = ctx.body().get("playerId");
            var playerName = ctx.body().get("playerName");
            var playerColorStr = ctx.body().get("playerColor");

            if (gameIdStr == null || playerIdStr == null || playerName == null || playerColorStr == null) {
                return ctx.respond("Missing gameId, playerId, playerName, or playerColor");
            }

            UUID gameId = UUID.fromString(gameIdStr);
            UUID playerId = UUID.fromString(playerIdStr);
            PlayerColors playerColor = PlayerColors.valueOf(playerColorStr);

            Player newPlayer = new Player();
            newPlayer.setPlayerId(playerId);
            newPlayer.setPlayerName(playerName);
            newPlayer.setColor(playerColor);

            return playerService.existsById(playerId)
                    .flatMap(exists -> {
                        if (exists) {
                            log.warn("Player with id {} already exists", playerId);
                            return ctx.respond("Internal Server Error");
                        } else {
                            return gameService.addPlayerToGame_Mono(newPlayer, gameId)
                                    .flatMap(ctx::respond);
                        }
                    })
                    .onErrorResume(e -> {
                        log.error("Error in handle(): {}", e.getMessage(), e);
                        return ctx.respond("Internal Server Error");
                    });

        } catch (Exception e) {
            log.error("Error in handle(): {}", e.getMessage(), e);
            return ctx.respond("Invalid input format.");
        }
    }


}
