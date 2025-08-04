package com.example.application.handlers.lobby;

import com.example.application.redis.RedisService_Mono;
import com.example.application.services.reactive.GameService_Mono;
import com.example.application.types.GameActions;
import com.example.application.utility.GameActionHandler;
import com.example.application.components.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartGame_Handler implements GameActionHandler {
    private final GameService_Mono gameService;
    private final RedisService_Mono redisService;

    @Override
    public String getAction() {
        return GameActions.START_GAME.name();
    }

    @Override
    public Mono<Void> handle(RequestContextRedis ctx) {
        try {
            UUID gameId = UUID.fromString(ctx.getBody().get("gameId"));
            return gameService.startGame_Mono(gameId)
                    .flatMap(e -> ctx.respond(e)
                            .then(redisService.publishGameUpd(e))
                    ).onErrorResume(e -> {
                        log.error("Error in handle(): {}", e.getMessage(), e);
                        return ctx.respond("Internal Server Error");
                    });
        } catch (Exception e) {
            log.error("Error in handle(): {}", e.getMessage(), e);
            return ctx.respond("Internal Server Error");
        }
    }
}
