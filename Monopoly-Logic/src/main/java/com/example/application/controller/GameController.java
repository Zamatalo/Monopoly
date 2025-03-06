package com.example.application.controller;


import com.example.application.components.GameSubscription;
import com.example.application.entity.Game;
import com.example.application.services.GameService;
import com.example.application.types.GameDTO;
import com.example.application.utility.GameMapper;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

//TODO exceptions
@Slf4j
@DgsComponent
public class GameController {
    private final GameService gameService;
    private final GameSubscription gameSubscription;
    private final Random random = new Random();

    public GameController(GameService gameService, GameSubscription gameSubscription) {
        this.gameService = gameService;
        this.gameSubscription = gameSubscription;
    }

    @DgsQuery
    public GameDTO findGameById(@InputArgument("id") String id) {
        Optional<Game> game = gameService.findById(UUID.fromString(id));
        assert game.isPresent();
        return GameMapper.INSTANCE.GameToGameDTO(game.get());
    }

    @DgsQuery
    public List<GameDTO> findAllGames() {
        return gameService.findAll().stream().map(GameMapper.INSTANCE::GameToGameDTO).collect(Collectors.toList());
    }

    @DgsMutation
    public String saveGame(@InputArgument("gameDto") GameDTO gameDto) {
        Game game = gameService.save(GameMapper.INSTANCE.GameDTOtoGame(gameDto));
        return game != null ? game.getGameId().toString() : null;
    }

    @DgsMutation
    public Boolean rollDice(@InputArgument("gameId") String gameId) {
        Optional<Game> gameOptional = gameService.findById(UUID.fromString(gameId));
        if (gameOptional.isEmpty()) {
            log.error("Game with id {} not found", gameId);
        }

        int randomNumber = random.nextInt(1, 7);
        Game game = gameOptional.get();
        GameDTO gameDto = GameMapper.INSTANCE.GameToGameDTO(game);

        int currentPlayerIndex = gameDto.getCurrentPlayerIndex();
        int newPosition = (gameDto.getPlayers().get(currentPlayerIndex).getPosition() + randomNumber) % 40;
        gameDto.getPlayers().get(currentPlayerIndex).setPosition(newPosition);

        int numberOfPlayers = gameDto.getPlayers().size();
        gameDto.setCurrentPlayerIndex((currentPlayerIndex + 1) % numberOfPlayers);

        Game updatedGame = GameMapper.INSTANCE.GameDTOtoGame(gameDto);
        gameService.save(updatedGame);

        gameSubscription.updateGame(gameDto);
        return true;
    }
}
