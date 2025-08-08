package com.example.application.handlers.lobby;

import com.example.application.components.RequestContextRedis;
import com.example.application.services.reactive.GameService_Mono;
import com.example.application.types.GameActions;
import com.example.application.utility.GameActionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;
/**
 *Handler responsible for processing the {@link GameActions#FIND_GAME_PLAYER_ID} game action.
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FindGame_ByPlayerId_Handler implements GameActionHandler {
    private final GameService_Mono gameService;

    @Override
    public String getAction() {
        return GameActions.FIND_GAME_PLAYER_ID.name();
    }

    @Override
    public Mono<Void> handle(RequestContextRedis ctx) {
        try {
            UUID playerId = UUID.fromString(ctx.body().get("playerId"));

            return gameService.findGameByPlayerId_Mono(playerId)
                    .flatMap(e-> e==null
                            ?ctx.respond("Internal Server Error")
                            :ctx.respond(e)
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
