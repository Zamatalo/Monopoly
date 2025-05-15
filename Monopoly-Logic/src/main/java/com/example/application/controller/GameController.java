package com.example.application.controller;

import com.example.application.components.GamePublisher;
import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.services.GameService;
import com.example.application.services.PlayerService;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerColors;
import com.example.application.util.PropertyData;
import com.example.application.utility.GameMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;
/// TODO: add proper error and exception handling
@Controller
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class GameController {
    private final GameService gameService;
    private final GamePublisher gamePublisher;
    private static final ConcurrentHashMap<UUID, CompletableFuture<Integer>> diceResults = new ConcurrentHashMap<>();
    private final PlayerService playerService;
    private final RedisClient redisClient;

    @MutationMapping
    public GameDTO createNewGame() {
        Game game = new Game();
        game = gameService.save(game);
        GameDTO gameDTO = GameMapper.INSTANCE.GameToGameDTO(game);
        gamePublisher.publish(gameDTO);
        return gameDTO;
    }

    @MutationMapping
    public GameDTO joinToGame(@Argument("gameId") UUID gameId,
                              @Argument("playerName") String playerName,
                              @Argument("playerColor") PlayerColors playerColor,
                              @Argument("playerId") UUID playerId) {
        Game game = gameService.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        Player player = new Player();
        player.setPlayerName(playerName);
        player.setPlayerId(playerId);
        player.setColor(com.example.application.util.enums.PlayerColors.valueOf(playerColor.toString()));

        gameService.addPlayerToGame(player, game);

        GameDTO gameDto = gameService.findById(gameId)
                .map(GameMapper.INSTANCE::GameToGameDTO)
                .orElseThrow(() -> new IllegalStateException("Failed to convert game to DTO"));

        gamePublisher.publish(gameDto);
        return gameDto;
    }

    @QueryMapping
    public List<GameDTO> getActiveGames() {
        return gameService.findAll().stream()
                .map(GameMapper.INSTANCE::GameToGameDTO)
                .collect(Collectors.toList());
    }

    @QueryMapping
    public GameDTO findGameById(@Argument("gameId") UUID id) {
        return gameService.findById(id)
                .map(GameMapper.INSTANCE::GameToGameDTO)
                .orElseThrow(() ->  new IllegalArgumentException("Game not found"));
    }

    @QueryMapping
    public GameDTO findGameByPlayerId(@Argument("playerId") UUID playerId) {
        return gameService.findGameByPlayerId(playerId)
                .map(GameMapper.INSTANCE::GameToGameDTO)
                .orElse(null);
    }

    @MutationMapping
    public GameDTO buyPropertyForPlayer(@Argument("gameId") UUID gameID) {
        var game = gameService.findById(gameID);
        if (game.isEmpty()) {
            throw new IllegalArgumentException("Game not found");
        }

        var playerId = game.get().getPlayers().get(game.get().getCurrentPlayerIndex()).getPlayerId();
        var player = playerService.findPlayer(playerId);
        if (player.isEmpty()) {
            throw new IllegalArgumentException("Player not found");
        }

        var whichCellIsPlayerStandingOn = PropertyData.ofPos(player.get().getPosition());
        player.get().addProperty(whichCellIsPlayerStandingOn);
        playerService.savePlayer(player.get());


        var updGame = GameMapper.INSTANCE.GameToGameDTO(gameService.findById(gameID).get());
        gamePublisher.publish(updGame);
        return updGame;
    }

    /**
     * TODO: write whats going on here
     */
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