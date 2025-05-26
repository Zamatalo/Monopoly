//package com.example.application.controller;
//
//import com.example.application.components.GamePublisher;
//import com.example.application.entity.Game;
//import com.example.application.entity.Player;
//import com.example.application.services.GameService;
//import com.example.application.services.PlayerService;
//import com.example.application.types.GameDTO;
//import com.example.application.util.PropertyData;
//import com.example.application.util.enums.GameState;
//import com.example.application.utility.GameMapper;
//import io.lettuce.core.RedisClient;
//import io.lettuce.core.api.StatefulRedisConnection;
//import io.lettuce.core.api.async.RedisAsyncCommands;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.graphql.data.method.annotation.Argument;
//import org.springframework.graphql.data.method.annotation.MutationMapping;
//import org.springframework.graphql.data.method.annotation.QueryMapping;
//import org.springframework.stereotype.Controller;
//
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.*;
//import java.util.stream.Collectors;
//
///// TODO: add proper error and exception handling
//@Controller
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
//@Slf4j
//public class GameController {
//    private final GameService gameService;
//    private final PlayerService playerService;
//    private final GamePublisher gamePublisher;
//    private static final ConcurrentHashMap<UUID, CompletableFuture<Integer>> diceResults = new ConcurrentHashMap<>();
//    private final RedisClient redisClient;
//    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
//
//    @QueryMapping
//    public List<GameDTO> getActiveGames() {
//        return gameService.findAll().stream()
//                .map(GameMapper.INSTANCE::GameToGameDTO)
//                .collect(Collectors.toList());
//    }
//
//    @QueryMapping
//    public GameDTO findGameById(@Argument("gameId") UUID id) {
//        return gameService.findById(id)
//                .map(GameMapper.INSTANCE::GameToGameDTO)
//                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
//    }
//
//    @QueryMapping
//    public GameDTO findGameByPlayerId(@Argument("playerId") UUID playerId) {
//        return gameService.findGameByPlayerId(playerId)
//                .map(GameMapper.INSTANCE::GameToGameDTO)
//                .orElse(null);
//    }
//
//    @MutationMapping
//    public GameDTO buyPropertyForPlayer(@Argument("gameId") UUID gameID, @Argument("playerId") UUID playerId) {
//        var game = gameService.findById(gameID)
//                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
//
//        if (!game.getGameState().equals(GameState.IN_PROGRESS)) {
//            throw new IllegalArgumentException("Game should be IN_PROGRESS");
//        }
//
//        var realPlayerId = game.getPlayers().get(game.getCurrentPlayerIndex()).getPlayerId();
//        if (!realPlayerId.equals(playerId)) {
//            throw new IllegalArgumentException("Wrong player id");
//        }
//
//        var player = playerService.findPlayer(playerId)
//                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
//
//
//        var whichCellIsPlayerStandingOn_Property = PropertyData.ofPos(player.getPosition());
//
//        var allBoughtPropertiesForGame = gameService.findProperties_AllPlayers_ForGame(gameID);
//        if (allBoughtPropertiesForGame.contains(whichCellIsPlayerStandingOn_Property)) {
//            throw new IllegalArgumentException("Property already bought");
//        }
//
//        player.addProperty(whichCellIsPlayerStandingOn_Property);
//        player.setBalance(player.getBalance() - whichCellIsPlayerStandingOn_Property.cost());
//        playerService.savePlayer(player);
//
//        var updGame = GameMapper.INSTANCE.GameToGameDTO(gameService.findById(gameID).get());
//        gamePublisher.publish(updGame);
//        return updGame;
//    }
//
//    /**
//     * TODO: write whats going on here
//     */
//    @MutationMapping
//    public CompletableFuture<Integer> rollDice(
//            @Argument("gameId") UUID gameId,
//            @Argument("playerId") UUID playerId) {
//
//        Game game = gameService.findById(gameId)
//                .orElseThrow(() -> {
//                    log.error("Game with id {} not found", gameId);
//                    return new IllegalArgumentException("Game not found");
//                });
//
//        if (!game.getGameState().equals(GameState.IN_PROGRESS)) {
//            throw new IllegalArgumentException("Game should be IN_PROGRESS");
//        }
//
//        Player player = playerService.findPlayer(playerId)
//                .orElseThrow(() -> {
//                    log.error("Player with id {} not found", playerId);
//                    return new IllegalArgumentException("Player not found");
//                });
//
//        if (game.getPlayers().isEmpty()) {
//            throw new IllegalStateException("No players in the game");
//        }
//
//        Player current = game.getPlayers().get(game.getCurrentPlayerIndex());
//        if (!player.getPlayerId().equals(current.getPlayerId())) {
//            throw new IllegalStateException("Wrong player id");
//        }
//
//        CompletableFuture<Integer> future = new CompletableFuture<>();
//        diceResults.put(gameId, future);
//
//        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
//            RedisAsyncCommands<String, String> asyncCommands = connection.async();
//            asyncCommands.publish("game:" + gameId + ":dice-roll-action", "");
//        }
//
//        scheduler.schedule(() -> {
//            if (!future.isDone()) {
//                future.completeExceptionally(new TimeoutException("Dice roll timed out"));
//                diceResults.remove(gameId);
//            }
//        }, 90, TimeUnit.SECONDS);
//
//        return future.thenCompose(topFace -> CompletableFuture.supplyAsync(() -> handleDiceRoll(gameId, topFace)));
//    }
//
//    private Integer handleDiceRoll(UUID gameId, int topFace) {
//        Game freshGame = gameService.findById(gameId)
//                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
//
//        Player freshPlayer = freshGame.getPlayers().get(freshGame.getCurrentPlayerIndex());
//
//        int newPosition = (freshPlayer.getPosition() + topFace) % 40;
//        freshGame.getPlayers().forEach(player -> {
//            player.getOwnedProperties().forEach(prop -> {
//                //stepped on another's player field
//                if (prop.isOwned() && prop.boardPosition() == newPosition) {
//                    freshPlayer.setBalance(freshPlayer.getBalance() - prop.cost());
//                }
//            });
//        });
//        freshPlayer.setPosition(newPosition);
//
//        int nextIndex = (freshGame.getCurrentPlayerIndex() + 1) % freshGame.getPlayers().size();
//
//
//        freshGame.setCurrentPlayerIndex(nextIndex);
//
//        gameService.save(freshGame);
//
//        GameDTO updatedDto = GameMapper.INSTANCE.GameToGameDTO(freshGame);
//        gamePublisher.publish(updatedDto);
//        isNextPlayerBot(freshGame, nextIndex);
//        return topFace;
//    }
//
//    public static void completeDiceFuture(UUID gameId, int topFace) {
//        CompletableFuture<Integer> future = diceResults.remove(gameId);
//        if (future != null) {
//            future.complete(topFace);
//        }
//    }
//
//    private void isNextPlayerBot(Game game, Integer nextPlayerIndex) {
//        var nextPlayer = game.getPlayers().get(nextPlayerIndex);
//
//        if (nextPlayer.isBot()) {
//
//            try {
//                TimeUnit.SECONDS.sleep(5);
//                rollDice(game.getGameId(), nextPlayer.getPlayerId());
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//
////    private String recieveMove_FromLLM(Game game) {
////        try {
////            var possibleMoves = this.getPossibleCurrentPlayerActions(game.getGameId());
////            var prompt =
////                    "Possible moves: " + possibleMoves
////                            + "For Game:" + new ObjectMapper().writeValueAsString(game);
////            var result = botService.decideMove(prompt);
////            System.out.println(result);
////            return result;
////        } catch (JsonProcessingException e) {
////            throw new RuntimeException(e);
////        }
////
////    }
//}