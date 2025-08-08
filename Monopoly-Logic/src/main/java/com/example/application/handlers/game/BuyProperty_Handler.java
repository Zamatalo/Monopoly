package com.example.application.handlers.game;

import com.example.application.redis.RedisService_Mono;
import com.example.application.services.reactive.GameService_Mono;
import com.example.application.services.reactive.PlayerService_Mono;
import com.example.application.services.reactive.TurnService_Mono;
import com.example.application.types.PlayerActions;
import com.example.application.util.data.PropertyData;
import com.example.application.utility.GameActionHandler;
import com.example.application.components.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;


/**
 * Handler responsible for processing the {@link PlayerActions#BUY_PROPERTY} game action.
 * <p>
 * This handler performs the following sequence of operations:
 * <ol>
 *     <li>Retrieves the {@code playerId} and {@code gameId} from the request context.</li>
 *     <li>Determines which property the player is currently on (based on position).</li>
 *     <li>Checks if the property is already owned by another player.</li>
 *     <li>If the property is available, adds it to the player's property list, updates the game state,
 *         and publishes the update via Redis.</li>
 *     <li>Finally, ends the player's turn by delegating to {@link TurnService_Mono#endTurn(UUID)}.</li>
 * </ol>
 *
 * <p>In case of an error (e.g., invalid UUID, property already bought, DB failure), it responds
 * with an Internal Server Error message via the Redis context.</p>
 *
 * @see TurnService_Mono#endTurn(UUID)
 */
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
                                            .flatMap(e -> redisService.publishGameUpd(e)
                                                    .then(ctx.respond(currentProperty))
                                                    .then(turnService.endTurn(gameId)));
                                });
                    })
                    .onErrorResume(e -> {
                        log.error("Error during BuyProperty_Handler:", e);
                        return ctx.respond("Internal server error");
                    });
        } catch (Exception e) {
            log.error("Invalid UUID format", e);
            return ctx.respond("Internal server error");
        }
    }

    private Mono<Boolean> isPropertyAlreadyBought(UUID gameId, PropertyData property) {
        return gameService.findAllProperties_Mono(gameId)
                .map(props -> props.contains(property));
    }
}
