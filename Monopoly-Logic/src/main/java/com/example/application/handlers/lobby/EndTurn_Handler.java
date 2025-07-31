package com.example.application.handlers.lobby;

import com.example.application.services.reactive.GameService_Mono;
import com.example.application.services.reactive.TurnService_Mono;
import com.example.application.types.PlayerActions;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EndTurn_Handler implements GameActionHandler {
    private final TurnService_Mono turnService;
    private final GameService_Mono gameService;

    @Override
    public String getAction() {
        return PlayerActions.END_TURN.toString();
    }

    @Override
    public Mono<Void> handle(RequestContextRedis ctx) {
        try {
            UUID gameId = UUID.fromString(ctx.body().get("gameId"));

            return turnService.endTurn(gameId)
                            .then(ctx.respond(
                                    gameService.findById_Mono(gameId))
                            );
        } catch (Exception e) {
            log.error(e.getMessage());
            return ctx.respond("Internal Server Error");
        }
    }
}