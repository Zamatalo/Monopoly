package com.example.application.controller;

import com.example.application.components.GamePublisher;
import com.example.application.entity.Game;
import com.example.application.entity.Player;
import com.example.application.services.GameService;
import com.example.application.types.GameDTO;
import com.example.application.types.PlayerColors;
import com.example.application.utility.GameMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
/// TODO: add proper error and exception handling
@Controller
@RequiredArgsConstructor
@Slf4j
public class GameController {
    private final GameService gameService;
    private final GamePublisher gamePublisher;

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
        player.setColor(com.example.application.PlayerColors.valueOf(playerColor.toString()));

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
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    }

    @QueryMapping
    public GameDTO findGameByPlayerId(@Argument("playerId") UUID playerId) {
        return gameService.findGameByPlayerId(playerId)
                .map(GameMapper.INSTANCE::GameToGameDTO)
                .orElse(null);
    }
}