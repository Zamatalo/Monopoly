package com.example.application.handlers.game;

import com.example.application.components.RequestContextRedis;
import com.example.application.config.GameConfig;
import com.example.application.redis.RedisService_Mono;
import com.example.application.services.reactive.DiceService_Mono;
import com.example.application.services.reactive.GameService_Mono;
import com.example.application.services.reactive.PlayerService_Mono;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerActions;
import com.example.application.types.PlayerDTO;
import com.example.application.types.PlayerState;
import com.example.application.util.data.PropertyData;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.GameMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Handler responsible for processing the {@link PlayerActions#ROLL_DICE} game action.

 * <ol>
 *     <li>Triggers a dice roll by sending a Redis message to the external Dice microservice.</li>
 *     <li>Waits for a response with the dice value (with a timeout fallback).</li>
 *     <li>If the player is in jail, they do not move; otherwise, their position is updated.</li>
 *     <li>If they pass the starting tile ("GO"), they receive a payout  {@link GameConfig#START_PAYOUT}.</li>
 *     <li>If the new tile is already owned by another player, rent is transferred.</li>
 *     <li>The player's state is updated to {@link PlayerState#AWAITING_DECISION}.</li>
 *     <li>The updated game state is saved and published to Redis.</li>
 * </ol>
 *
 *
 * <p><strong>Note:</strong> This handler only handles the immediate consequences of a dice roll.
 * It does not handle property purchase or special tile logic like jail or chance cards.
 *
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RollDice_Handler implements GameActionHandler {
    private final DiceService_Mono diceService;
    private final GameService_Mono gameService;
    private final PlayerService_Mono playerService;
    private final RedisService_Mono redisService;

    @Override
    public String getAction() {
        return PlayerActions.ROLL_DICE.name();
    }

    @Override
    public Mono<Void> handle(RequestContextRedis ctx) {
        UUID gameId = UUID.fromString(ctx.body().get("gameId"));
        UUID playerId = UUID.fromString(ctx.body().get("playerId"));

        // sending request to diceserver and waiting for response
        return diceService.rollDice(gameId.toString())
                .timeout(Duration.ofSeconds(10), Mono.just(1))
                .flatMap(rolledResult -> processDiceRoll(gameId, playerId, rolledResult, ctx))
                .onErrorResume(e -> {
                    log.error("Error handling dice roll", e);
                    return ctx.respond("Internal Server Error");
                });
    }

    private Mono<Void> processDiceRoll(UUID gameId, UUID playerId, int rolledResult, RequestContextRedis ctx) {
        return Mono.zip(
                gameService.findById_Mono(gameId),
                playerService.findById(playerId)
        ).flatMap(tuple -> {
            GameDTO game = tuple.getT1();
            PlayerDTO currentPlayer = tuple.getT2();

            // calculating new position
            int finalRolledResult = currentPlayer.getInJail_Turns() > 0 ? 0 : rolledResult;
            int newPosition = (currentPlayer.getPosition() + finalRolledResult) % 40;

            // if stepped on start
            if (passedGo(currentPlayer.getPosition(), newPosition)) {
                currentPlayer.setBalance(currentPlayer.getBalance() + GameConfig.START_PAYOUT);
            }

            // new state for player
            currentPlayer.setPosition(newPosition);
            currentPlayer.setPlayerState(PlayerState.AWAITING_DECISION);

            return steppedOnAnotherPlayerField(gameId, currentPlayer)
                    // if stepped, subtracting from player's balance
                    .flatMap(property -> playerService.getPlayer_forProperty_forGame(gameId, property)
                            .doOnNext(propertyOwner -> {
                                int rent = property.cost();
                                currentPlayer.setBalance(currentPlayer.getBalance() - rent);
                                propertyOwner.setBalance(propertyOwner.getBalance() + rent);
                            })
                            .thenReturn(game))
                    // if not, just returning game
                    .defaultIfEmpty(game)
                    //  updating game with new val's
                    .flatMap(updatedGame -> {
                        List<PlayerDTO> updatedPlayers = updatedGame.getPlayers().stream()
                                .map(p -> p.getPlayerId().equals(currentPlayer.getPlayerId()) ? currentPlayer : p)
                                .toList();
                        updatedGame.setPlayers(updatedPlayers);
                        return gameService.save_Mono(GameMapper.INSTANCE.GameDTOtoGame(updatedGame))
                                .flatMap(savedGame -> redisService.publishGameUpd(savedGame)
                                        .then(ctx.respond(rolledResult)));
                    });
        });
    }

    private Mono<PropertyData> steppedOnAnotherPlayerField(UUID gameId, PlayerDTO currentPlayer) {
        return gameService.findAllProperties_Mono(gameId)
                .flatMap(properties -> Mono.justOrEmpty(
                        properties.stream()
                                .filter(p -> p.boardPosition() == currentPlayer.getPosition() &&
                                        !p.ownerId().equals(UUID.fromString(currentPlayer.getPlayerId())))
                                .findFirst()
                                .orElse(null)
                ));
    }

    private boolean passedGo(Integer prevPos, Integer nextPos) {
        return nextPos < prevPos;
    }
}