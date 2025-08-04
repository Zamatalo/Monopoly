package com.example.application.handlers.lobby;

import com.example.application.entity.Game;
import com.example.application.services.reactive.GameService_Mono;
import com.example.application.types.GameActions;
import com.example.application.utility.GameActionHandler;
import com.example.application.components.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class CreateGame_Handler implements GameActionHandler {
    private final GameService_Mono gameService;

    @Override
    public String getAction() {
        return GameActions.CREATE_GAME.name();
    }

    @Override
    public Mono<Void> handle(RequestContextRedis ctx) {
        try {
            return gameService.save_Mono(new Game())
                    .flatMap(ctx::respond)
                    .onErrorResume(e -> {
                        log.error("Error in handle(): {}", e.getMessage(), e);
                        return ctx.respond(e.getMessage());
                    });
        } catch (Exception e) {
            log.error("Error in handle(): {}", e.getMessage(), e);
            return ctx.respond("Internal Server Error");
        }
    }
}
