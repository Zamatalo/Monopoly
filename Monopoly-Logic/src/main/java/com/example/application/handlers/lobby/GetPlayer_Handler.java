package com.example.application.handlers.lobby;

import com.example.application.components.GameActionResolver;
import com.example.application.services.reactive.GameService_Mono;
import com.example.application.services.reactive.PlayerService_Mono;
import com.example.application.types.GameActions;
import com.example.application.utility.GameActionHandler;
import com.example.application.components.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GetPlayer_Handler implements GameActionHandler {
    private final PlayerService_Mono playerService;
    private final GameService_Mono gameService;

    @Override
    public String getAction() {
        return GameActions.GET_PLAYER.name();
    }

    @Override
    public Mono<Void> handle(RequestContextRedis ctx) {
        try {
            var playerId = UUID.fromString(ctx.getBody().get("playerId"));
            var gameId = UUID.fromString(ctx.getBody().get("gameId"));

            return playerService.findById(playerId)
                    .flatMap(playerDTO ->
                            gameService.findById_Mono(gameId)
                                    .flatMap(gameDTO -> {
                                        playerDTO.setPlayerActions(GameActionResolver.resolvePlayerActions(gameDTO, playerDTO));
                                        return ctx.respond(playerDTO);
                                    })
                    )
                    .onErrorResume(e -> {
                        log.error("Error in handle(): {}", e.getMessage(), e);
                        return ctx.respond("Internal Server Error");
                    });

        } catch (Exception e) {
            log.error("Error in handle(): {}", e.getMessage(), e);
            return ctx.respond("Internal Server Error");
        }
    }
}
