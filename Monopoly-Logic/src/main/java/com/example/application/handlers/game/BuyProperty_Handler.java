package com.example.application.handlers.game;

import com.example.application.services.reactive.GameService_Mono;
import com.example.application.services.imperative.PlayerService;
import com.example.application.services.reactive.RedisService_Mono;
import com.example.application.services.TurnService;
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
    private final PlayerService playerService;
    private final RedisService_Mono redisService;
    private final TurnService turnService;

    @Override
    public String getAction() {
        return "BUY_PROPERTY";
    }

    @Override
    public Mono<Void> handle(RequestContextRedis ctx) {
        UUID gameId = UUID.fromString(ctx.body().get("gameId"));
        UUID playerId = UUID.fromString(ctx.body().get("playerId"));

        return playerService.findByIdMono(playerId)
                .flatMap(player -> {
                    PropertyData currentProperty = PropertyData.ofPos(player.getPosition());
                    return isPropertyAlreadyBought(gameId, currentProperty)
                            .flatMap(alreadyBought -> {
                                if (alreadyBought) {
                                    ctx.respond("Property already bought");
                                    return Mono.error(new IllegalArgumentException("Property already bought"));
                                }

                                return playerService.addPropertyToPlayerMono(playerId, currentProperty)
                                        .then(gameService.findById_Mono(gameId))
                                        .flatMap(redisService::publishGameUpd) // publishGameUpd should return Mono<Void>
                                        .then(isFromBot(ctx))
                                        .flatMap(fromBot -> {
                                            if (!fromBot) {
                                                ctx.respond(currentProperty);
                                            }
                                            return turnService.endTurnMono(gameId); // <-- this also should return Mono<Void>
                                        });
                            });
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty(); // or ctx.respond("Error: " + e.getMessage()) if needed
                });
    }


    /// if its from bot, record should be ack
    /// later should be dedicated channel for bot calls?
    public Mono<Boolean> isFromBot(RequestContextRedis ctx) {
        var a = ctx.body().get("isFromBot");
        var b = a != null && a.equals("true");
        return Mono.just(b);
    }
    private Mono<Boolean> isPropertyAlreadyBought(UUID gameId, PropertyData target) {
        return gameService.findAllProperties_Mono(gameId)
                .map(props -> props.contains(target));
    }

}
