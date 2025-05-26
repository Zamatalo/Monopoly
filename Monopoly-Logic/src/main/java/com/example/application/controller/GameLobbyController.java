//package com.example.application.controller;
//
//import com.example.application.components.GamePublisher;
//import com.example.application.entity.Game;
//import com.example.application.entity.Player;
//import com.example.application.services.GameService;
//import com.example.application.types.GameDTO;
//import com.example.application.util.enums.GameState;
//import com.example.application.util.enums.PlayerColors;
//import com.example.application.utility.GameMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.graphql.data.method.annotation.Argument;
//import org.springframework.graphql.data.method.annotation.MutationMapping;
//import org.springframework.stereotype.Controller;
//
//import java.time.LocalTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.UUID;
//
//@Controller
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
//@Slf4j
//public class GameLobbyController {
//    private final GameService gameService;
//    private final GamePublisher gamePublisher;
//
//    @MutationMapping
//    public GameDTO createNewGame() {
//        Game game = new Game();
//
//        game = gameService.save(game);
//        var gameDTO = GameMapper.INSTANCE.GameToGameDTO(game);
//        gamePublisher.publish(gameDTO);
//        return gameDTO;
//    }
//
//    @MutationMapping
//    public GameDTO joinToGame(@Argument("gameId") UUID gameId,
//                              @Argument("playerName") String playerName,
//                              @Argument("playerColor") PlayerColors playerColor,
//                              @Argument("playerId") UUID playerId) {
//        Game game = gameService.findById(gameId)
//                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
//
//        Player player = new Player();
//        player.setPlayerName(playerName);
//        player.setPlayerId(playerId);
//        player.setColor(PlayerColors.valueOf(playerColor.toString()));
//
//        gameService.addPlayerToGame(player, game);
//
//        GameDTO gameDto = gameService.findById(gameId)
//                .map(GameMapper.INSTANCE::GameToGameDTO)
//                .orElseThrow(() -> new IllegalStateException("Failed to convert game to DTO"));
//
//        gamePublisher.publish(gameDto);
//        return gameDto;
//    }
//
//    @MutationMapping
//    public GameDTO startGame(@Argument("gameId") UUID gameId) {
//        Game game = gameService.findById(gameId)
//                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
//
//        if (!(game.getPlayers().size() == 4) || !game.getGameState().equals(GameState.STARTED)) {
//            return GameMapper.INSTANCE.GameToGameDTO(game);
//        }
//
//        game.setGameState(GameState.IN_PROGRESS);
//        game.setCurrentPlayerIndex(0);
//        gameService.save(game);
//
//        GameDTO gameDTO = GameMapper.INSTANCE.GameToGameDTO(game);
//        gamePublisher.publish(gameDTO);
//        return gameDTO;
//    }
//
//    @MutationMapping
//    public GameDTO addBotToGame(@Argument("gameId") UUID gameId) {
//        Game game = gameService.findById(gameId)
//                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
//
//
//        if ((game.getPlayers().size() >= 4) || !game.getGameState().equals(GameState.STARTED)) {
//            throw  new IllegalArgumentException("Already 4 players");
//        }
//
//        var allColors = PlayerColors.values();
//        var usedColors = new ArrayList<>();
//
//        game.getPlayers().forEach(p -> usedColors.add(p.getColor()));
//
//        var color = Arrays.stream(allColors)
//                .filter(color1 -> !usedColors.contains(color1))
//                .findFirst();
//
//        if (color.isEmpty()) {
//            throw new IllegalArgumentException("Color not found");
//        }
//
//        Player bot = new Player();
//        bot.setPlayerId(UUID.randomUUID());
//        bot.setPlayerName("Bot "+ LocalTime.now().getSecond());
//        bot.setBot(true);
//        bot.setColor(color.get());
//
//        gameService.addPlayerToGame(bot,game);
//        var gameDto = GameMapper.INSTANCE.GameToGameDTO(gameService.findById(gameId).get());
//        gamePublisher.publish(gameDto);
//        return gameDto;
//    }
//}
