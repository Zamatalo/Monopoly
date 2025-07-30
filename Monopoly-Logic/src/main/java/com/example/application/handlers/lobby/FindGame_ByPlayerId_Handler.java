package com.example.application.handlers.lobby;

import com.example.application.services.reactive.GameService_Mono;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FindGame_ByPlayerId_Handler implements GameActionHandler {
    private final GameService_Mono gameService;

    @Override
    public String getAction() {
        return "findGameByPlayerId";
    }

    @Override
    public Mono<Void> handle(RequestContextRedis ctx) {
        String playerIdRaw = ctx.body().get("playerId");
        if (playerIdRaw.isBlank()) {
            return ctx.respond("Missing playerId");
        }
        UUID playerId = UUID.fromString(playerIdRaw);

        return gameService.findGameByPlayerId_Mono(playerId)
                .flatMap(ctx::respond)
                .onErrorResume(e -> {
                    log.error("Error in findGameByPlayerId handler", e);
                    return ctx.respond("Internal Server Error");
                });
    }
}
