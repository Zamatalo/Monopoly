package com.example.application.handlers.game;

import com.example.application.config.GameConfig;
import com.example.application.services.reactive.*;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerActions;
import com.example.application.types.PlayerDTO;
import com.example.application.types.PlayerState;
import com.example.application.util.data.PropertyData;
import com.example.application.utility.GameActionHandler;
import com.example.application.utility.GameMapper;
import com.example.application.utility.RequestContextRedis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RollDice_Handler implements GameActionHandler {
    private final DiceService_Mono diceService;
    private final GameService_Mono gameService;
    private final PlayerService_Mono playerService;
    private final RedisService_Mono redisService;
    private final BotService_Mono botService;

    @Override
    public String getAction() {
        return PlayerActions.ROLL_DICE.name();
    }

    @Override
    public Mono<Void> handle(RequestContextRedis ctx) {
        UUID gameId = UUID.fromString(ctx.body().get("gameId"));
        UUID playerId = UUID.fromString(ctx.body().get("playerId"));

        /// sending request to diceserver and waiting for response
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


            /// calculating new position
            int finalRolledResult = currentPlayer.getInJail_Turns() > 0 ? 0 : rolledResult;
            int newPosition = (currentPlayer.getPosition() + finalRolledResult) % 40;

            /// if stepped on start
            if (passedGo(currentPlayer.getPosition(), newPosition)) {
                currentPlayer.setBalance(currentPlayer.getBalance() + GameConfig.START_PAYOUT);
            }

            /// new state for player
            currentPlayer.setPosition(newPosition);
            currentPlayer.setPlayerState(PlayerState.AWAITING_DECISION);

            return steppedOnAnotherPlayerField(gameId, currentPlayer)
                    /// if stepped, subtracting from player's balance
                    .flatMap(property -> playerService.getPlayer_forProperty_forGame(gameId, property)
                            .doOnNext(propertyOwner -> {
                                int rent = property.cost();
                                currentPlayer.setBalance(currentPlayer.getBalance() - rent);
                                propertyOwner.setBalance(propertyOwner.getBalance() + rent);
                            })
                            .thenReturn(game))
                    /// if not, just returning game
                    .defaultIfEmpty(game)
                    ///  updating game with new val's
                    .flatMap(updatedGame -> {
                        List<PlayerDTO> updatedPlayers = updatedGame.getPlayers().stream()
                                .map(p -> p.getPlayerId().equals(currentPlayer.getPlayerId()) ? currentPlayer : p)
                                .toList();
                        updatedGame.setPlayers(updatedPlayers);
                        return gameService.save_Mono(GameMapper.INSTANCE.GameDTOtoGame(updatedGame))
                                .flatMap(savedGame -> redisService.publishGameUpd(savedGame)
                                        .then(respondToClient(savedGame, ctx,rolledResult)));
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

    private Mono<Void> respondToClient(GameDTO game, RequestContextRedis ctx,int rolledResult) {
        if ("true".equals(ctx.body().get("sentFromBot"))) {
            return botService.handleAfterRollAction(game);
        }
        return ctx.respond(rolledResult)
                .then(redisService.publishGameUpd(game));
    }

    private boolean passedGo(Integer prevPos, Integer nextPos) {
        return nextPos < prevPos;
    }
}