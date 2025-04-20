package com.example.application.controller;

import com.example.application.components.DicePublisher;
import com.example.application.components.GamePublisher;
import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.services.GameService;
import com.example.application.services.PlayerService;
import com.example.application.types.DicePosition;
import com.example.application.types.GameDTO;
import com.example.application.utility.GameMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;
import java.util.concurrent.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GameSubscription {
    private final GamePublisher gamePublisher;
    private final RedisClient redisClient;
    private final DicePublisher dicePublisher;
    private static final ConcurrentHashMap<UUID, CompletableFuture<Integer>> diceResults = new ConcurrentHashMap<>();
    private final GameService gameService;
    private final PlayerService playerService;

    @SubscriptionMapping
    public Publisher<GameDTO> gameUpdated(@Argument("gameId") String gameId) {
        return gamePublisher.getPublisherForGame(gameId);
    }

    @SubscriptionMapping
    public Publisher<DicePosition> diceUpdated(@Argument("gameId") String gameId) {
        return dicePublisher.getPublisherForGame(gameId);
    }

    /// TODO:
    ///     write whats going on here
    @MutationMapping
    public CompletableFuture<Integer> rollDice(
            @Argument("gameId") UUID gameId,
            @Argument("playerId") UUID playerId) {

        Game game = gameService.findById(gameId)
                .orElseThrow(() -> {
                    log.error("Game with id {} not found", gameId);
                    return new IllegalArgumentException("Game not found");
                });

        Player player = playerService.findPlayer(playerId)
                .orElseThrow(() -> {
                    log.error("Player with id {} not found", playerId);
                    return new IllegalArgumentException("Player not found");
                });

        if (game.getPlayers().isEmpty()) {
            throw new IllegalStateException("No players in the game");
        }

        Player current = game.getPlayers().get(game.getCurrentPlayerIndex());

        if (!player.getPlayerId().equals(current.getPlayerId())) {
            throw new IllegalStateException("Wrong player id");
        }

        CompletableFuture<Integer> future = new CompletableFuture<>();
        diceResults.put(gameId, future);

        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisAsyncCommands<String, String> asyncCommands = connection.async();
            asyncCommands.publish("game:" + gameId + ":dice-roll-action", "");
        }

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.schedule(() -> {
            if (!future.isDone()) {
                future.completeExceptionally(new TimeoutException("Dice roll timed out"));
                diceResults.remove(gameId);
            }
        }, 60, TimeUnit.SECONDS);

        return future.thenCompose(topFace -> CompletableFuture.supplyAsync(() -> {
            Game freshGame = gameService.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("Game not found"));

            Player freshCurrent = freshGame.getPlayers().get(freshGame.getCurrentPlayerIndex());
            int newPosition = (freshCurrent.getPosition() + topFace) % 40;
            freshCurrent.setPosition(newPosition);

            int nextIndex = (freshGame.getCurrentPlayerIndex() + 1) % freshGame.getPlayers().size();
            freshGame.setCurrentPlayerIndex(nextIndex);

            gameService.save(freshGame);

            GameDTO updatedDto = GameMapper.INSTANCE.GameToGameDTO(freshGame);
            gamePublisher.publish(updatedDto);

            return topFace;
        }));


    }

    public static void completeDiceFuture(UUID gameId, int topFace) {
        CompletableFuture<Integer> future = diceResults.remove(gameId);
        if (future != null) {
            future.complete(topFace);
        }
    }
}