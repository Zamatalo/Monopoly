//package com.example.application.resolver;
//
//import com.example.application.dto.GameDTO;
//import com.example.application.entity.Game;
//import com.example.application.services.GameService;
//import com.example.application.utility.GameMapper;
//
//import com.netflix.graphql.dgs.DgsComponent;
//import com.netflix.graphql.dgs.DgsQuery;
//import com.netflix.graphql.dgs.InputArgument;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@DgsComponent
//public class GameResolver {
//
//    private final GameService gameService;
//
//    public GameResolver(GameService gameService) {
//        this.gameService = gameService;
//    }
//
//    @DgsQuery
//    public GameDTO findGameById(@InputArgument("id") String id) {
//        Optional<Game> game =gameService.findById(UUID.fromString(id));
//        return GameMapper.INSTANCE.GameToGameDTO(game.orElse(null));
//
//    }
//
//}
