package com.example.application.handlers.lobby;

import com.example.application.services.reactive.GameService_Mono;
import com.example.application.types.GameActions;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class FindGameById_Handler implements GameActionHandler {
    private final GameService_Mono gameService;

    @Override
    public String getAction() {
        return GameActions.FIND_GAME_BY_ID.name();
    }

    @Override
    public Mono<Void> handle(RequestContextRedis ctx) {
        try {
            var gameId = UUID.fromString(ctx.body().get("gameId"));

            return gameService.findById_Mono(gameId)
                    .flatMap(ctx::respond)
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
