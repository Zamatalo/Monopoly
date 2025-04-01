package com.example.application.controller;


import com.example.application.entity.Game;
import com.example.application.services.GameService;
import com.example.application.types.GameDTO;
import com.example.application.utility.GameMapper;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

//TODO exceptions
@Slf4j
@DgsComponent
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
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

//    @DgsMutation
//    public String saveGame(@InputArgument("gameDTOInput") GameDTOInput gameDto) {
//        Game game = gameService.save(GameMapper.INSTANCE.GameDTOtoGame(gameDto));
//        return game != null ? game.getGameId().toString() : null;
//    }



}
