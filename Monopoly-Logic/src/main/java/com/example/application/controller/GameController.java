package com.example.application.controller;


import com.example.application.dto.GameDTO;
import com.example.application.entity.Game;
import com.example.application.services.GameService;
import com.example.application.utility.GameMapper;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@DgsComponent
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

//    @Override
//    public GraphQlResponse findGameById(GraphQlRequest id) {
//        GraphQLQueryRequest
//    }


//    @MutationMapping
//    public Game startGame(@Argument("playerNames") List<String> playerNames) {
//        Game game = new Game();
//        game.setPlayers(playerNames.stream().map(name -> {
//            Player player = new Player();
//            player.setName(name);
//            return player;
//        }).toList());
//        game.setCurrentPlayerIndex(0);
//        game.setGameState(GameState.IN_PROGRESS);
//        return gameService.save(game);
//    }

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
//
//    @QueryMapping
//    @Override
//    public List<GameDTO> findAllGames() {
//        List<Game> games = gameService.findAll();
//        assert games != null;
//        return games.stream()
//                .map(GameMapper.INSTANCE::GameToGameDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    @MutationMapping
//    public Boolean saveGame(@Argument("game") GameDTO gameDTO) {
//        Game game= gameService.save(GameMapper.INSTANCE.GameDTOtoGame(gameDTO));
//        return game != null;
//    }


}
