package com.example.application.handlers.lobby;

import com.example.application.services.reactive.GameService_Mono;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GetAllGames_Handler implements GameActionHandler {
    private final GameService_Mono gameService;

    @Override
    public String getAction() {
        return "getAllGames";
    }

    @Override
    public Mono<Void> handle(RequestContextRedis ctx) {
        return gameService.findAll_Mono()
                .flatMap(ctx::respond)
                .onErrorResume(e -> {
                    log.error("Error in getAllGames handler", e);
                    return ctx.respond("Internal Server Error");
                });
    }


}
