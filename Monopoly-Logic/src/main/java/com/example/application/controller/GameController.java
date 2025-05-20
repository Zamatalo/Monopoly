package com.example.application.controller;

import com.example.application.components.GamePublisher;
import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.services.GameService;
import com.example.application.services.PlayerService;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerColors;
import com.example.application.util.PropertyData;
import com.example.application.util.enums.GameState;
import com.example.application.util.enums.PlayerActions;
import com.example.application.utility.GameMapper;
import com.example.application.utility.TurnTimerManager;
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
    private final PlayerService playerService;
    private final GamePublisher gamePublisher;
    private static final ConcurrentHashMap<UUID, CompletableFuture<Integer>> diceResults = new ConcurrentHashMap<>();
    private final RedisClient redisClient;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

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

    @MutationMapping
    public GameDTO startGame(@Argument("gameId") UUID gameId) {
        Game game = gameService.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (!(game.getPlayers().size() == 4) || !game.getGameState().equals(GameState.STARTED)) {
            return GameMapper.INSTANCE.GameToGameDTO(game);
        }

        game.setGameState(GameState.IN_PROGRESS);
        game.setCurrentPlayerIndex(0);
        gameService.save(game);

        GameDTO gameDTO = GameMapper.INSTANCE.GameToGameDTO(game);
        gamePublisher.publish(gameDTO);
        return gameDTO;
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

        if (!game.get().getGameState().equals(GameState.IN_PROGRESS)) {
            throw new IllegalArgumentException("Game should be IN_PROGRESS");
        }

        var playerId = game.get().getPlayers().get(game.get().getCurrentPlayerIndex()).getPlayerId();
        var player = playerService.findPlayer(playerId);
        if (player.isEmpty()) {
            throw new IllegalArgumentException("Player not found");
        }

        var whichCellIsPlayerStandingOn_Property = PropertyData.ofPos(player.get().getPosition());
        var allPropertiesForGame = gameService.findProperties_AllPlayers_ForGame(gameID);
        if (allPropertiesForGame.contains(whichCellIsPlayerStandingOn_Property)) {
            throw new IllegalArgumentException("Property already bought");
        }

        player.get().addProperty(whichCellIsPlayerStandingOn_Property);
        player.get().setBalance(player.get().getBalance() - whichCellIsPlayerStandingOn_Property.cost());
        playerService.savePlayer(player.get());

        var updGame = GameMapper.INSTANCE.GameToGameDTO(gameService.findById(gameID).get());
        gamePublisher.publish(updGame);
        return updGame;
    }

    @QueryMapping
    public List<PlayerActions> getPossibleCurrentPlayerActions(@Argument("gameId") UUID gameId) {
        var game = gameService.findById(gameId);
        if (game.isEmpty()) {
            throw new IllegalArgumentException("Game not found");
        }

        if (!game.get().getGameState().equals(GameState.IN_PROGRESS)) {
            return List.of(PlayerActions.START_GAME);
        }


        var playerId = game.get().getPlayers().get(game.get().getCurrentPlayerIndex()).getPlayerId();
        var player = playerService.findPlayer(playerId);
        if (player.isEmpty()) {
            throw new IllegalArgumentException("Player not found");
        }


        return List.of(PlayerActions.BUY_PROPERTY);
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

        if (!game.getGameState().equals(GameState.IN_PROGRESS)) {
            throw new IllegalArgumentException("Game should be IN_PROGRESS");
        }

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


        scheduler.schedule(() -> {
            if (!future.isDone()) {
                future.completeExceptionally(new TimeoutException("Dice roll timed out"));
                diceResults.remove(gameId);
            }
        }, 90, TimeUnit.SECONDS);

        return future.thenCompose(topFace ->
                CompletableFuture.supplyAsync(() -> handleDiceRoll(gameId, topFace)));


    }

    private Integer handleDiceRoll(UUID gameId, int topFace) {
        Game freshGame = gameService.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        Player freshPlayer = freshGame.getPlayers().get(freshGame.getCurrentPlayerIndex());

        int newPosition = (freshPlayer.getPosition() + topFace) % 40;
        freshGame.getPlayers().forEach(player -> {
            player.getOwnedProperties().forEach(prop -> {
                //stepped on another's player field
                if (prop.isOwned() && prop.boardPosition() == newPosition) {
                    freshPlayer.setBalance(freshPlayer.getBalance() - prop.cost());
                }
            });
        });
        freshPlayer.setPosition(newPosition);

        int nextIndex = (freshGame.getCurrentPlayerIndex() + 1) % freshGame.getPlayers().size();
        freshGame.setCurrentPlayerIndex(nextIndex);

        gameService.save(freshGame);

        GameDTO updatedDto = GameMapper.INSTANCE.GameToGameDTO(freshGame);
        gamePublisher.publish(updatedDto);

        return topFace;
    }

    public static void completeDiceFuture(UUID gameId, int topFace) {
        CompletableFuture<Integer> future = diceResults.remove(gameId);
        if (future != null) {
            future.complete(topFace);
        }
    }


}