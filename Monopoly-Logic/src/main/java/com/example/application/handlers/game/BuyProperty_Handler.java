package com.example.application.handlers.game;

import com.example.application.services.reactive.GameService_Mono;
import com.example.application.services.reactive.PlayerService_Mono;
import com.example.application.services.reactive.RedisService_Mono;
import com.example.application.services.reactive.TurnService_Mono;
import com.example.application.types.PlayerActions;
import com.example.application.util.data.PropertyData;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BuyProperty_Handler implements GameActionHandler {
    private final GameService_Mono gameService;
    private final PlayerService_Mono playerService;
    private final RedisService_Mono redisService;
    private final TurnService_Mono turnService;

    @Override
    public String getAction() {
        return PlayerActions.BUY_PROPERTY.name();
    }

    @Override
    public Mono<Void> handle(RequestContextRedis ctx) {

        try {
            UUID gameId = UUID.fromString(ctx.body().get("gameId"));
            UUID playerId = UUID.fromString(ctx.body().get("playerId"));
            return playerService.findById(playerId)
                    .flatMap(player -> {
                        PropertyData currentProperty = PropertyData.ofPos(player.getPosition());
                        return isPropertyAlreadyBought(gameId, currentProperty)
                                .flatMap(alreadyBought -> {
                                    if (alreadyBought) {
                                        return ctx.respond("Internal Server Error");
                                    }

                                    return playerService.addPropertyToPlayer(playerId, currentProperty)
                                            .then(gameService.findById_Mono(gameId))
                                            .flatMap(redisService::publishGameUpd)
                                            .then(isFromBot(ctx))
                                            .flatMap(fromBot -> {
                                                if (!fromBot) {
                                                    return ctx.respond(currentProperty);
                                                } else {
                                                    return Mono.empty();
                                                }
                                            })
                                            .then(turnService.endTurn(gameId));
                                });
                    })
                    .onErrorResume(e -> {
                        log.error("Error during BuyProperty_Handler:", e);
                        return ctx.respond("Internal server error during property purchase.");
                    });
        } catch (Exception e) {
            log.error("Invalid UUID format", e);
            return ctx.respond("Invalid gameId or playerId format.");
        }


    }

    private Mono<Boolean> isFromBot(RequestContextRedis ctx) {
        String flag = ctx.body().get("sentFromBot");
        return Mono.just("true".equals(flag));
    }

    private Mono<Boolean> isPropertyAlreadyBought(UUID gameId, PropertyData property) {
        return gameService.findAllProperties_Mono(gameId)
                .map(props -> props.contains(property));
    }
}
